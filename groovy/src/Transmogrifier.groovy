import groovy.sql.Sql;
import model.Author;
import model.Doi;
import model.Publication;
import model.Publisher;
import model.Url;


class Transmogrifier {
    
    def sql = Sql.newInstance("jdbc:mysql://localhost/deploader", "root", "root")
    def dois = sql.dataSet('dois')
    def publications = sql.dataSet('publications')
    def authors = sql.dataSet('authors')
    def publishers = sql.dataSet('publishers')
    def urls = sql.dataSet('urls')
    def publishersDois = sql.dataSet('publishersDois')
    def publishersPublications = sql.dataSet('publishersPublications')
    
    def setupTables() {
        sql.execute '''
            CREATE TABLE publications (
                id int(10) unsigned not null auto_increment,
                title varchar(50) default null,
                pIssn varchar(50) default null,
                eIssn varchar(50) default null,
                type varchar(50) default null,
                primary key (id))
            DEFAULT CHARSET = utf8;
        '''
        
        sql.execute '''
            CREATE TABLE dois (
                id int(10) unsigned not null auto_increment,
                xml text,
                doi varchar(255) default null,
                articleTitle text,
                owner varchar(50) default null,
                citationId varchar(50) default null,
                dateStamp varchar(50) default null,
                fileDate varchar(50) default null,
                volume varchar(50) default null,
                issue varchar(50) default null,
                year varchar(50) default null,
                month varchar(50) default null,
                day varchar(50) default null,
                firstPage varchar(50) default null,
                lastPage varchar(50) default null,
                dumpFilename varchar(50) default null,
                publicationId int(10) unsigned not null,
                primary key (id),
                unique key unique_dois_doi (doi),
                key index_dois_doi (doi))
            DEFAULT CHARSET = utf8;
        '''
        
        sql.execute '''
            CREATE TABLE urls (
                id int(10) unsigned not null auto_increment,
                uri text default null,
                host varchar(50) default null,
                doiId int(10) unsigned not null,
                primary key (id),
                key index_urls_doi (doiId))
            DEFAULT CHARSET = utf8;
        '''
        
        sql.execute '''
            CREATE TABLE authors (
                id int(10) unsigned not null auto_increment,
                surname varchar(255) default null,
                givenName varchar(255) default null,
                sequence varchar(50) default null,
                contributorRole varchar(5) default null,
                doiId int(10) unsigned not null,
                primary key (id),
                key index_authors_doi (doiId))
            DEFAULT CHARSET = utf8;
        '''
        
        sql.execute '''
            CREATE TABLE publishers (
                id int(10) unsigned not null auto_increment,
                name varchar(255) default null,
                location varchar(255) default null,
                primary key (id),
                key index_publishers_name (name))
            DEFAULT CHARSET = utf8;
        '''
        
        sql.execute '''
            CREATE TABLE publishersDois (
                publisherId int(10) unsigned not null,
                doiId int(10) unsigned not null,
                primary key (publisherId, doiId))
            DEFAULT CHARSET = utf8;
        '''
        
        sql.execute '''
            CREATE TABLE publishersPublications (
                publisherId int(10) unsigned not null,
                publicationId int(10) unsigned not null,
                primary key (publisherId, publicationId))
            DEFAULT CHARSET = utf8;
        '''
    }
    
    def transmogrify(root, dumpFile) {
        
        sql.withTransaction {
            def publicationId = sql.firstRow("SELECT COUNT(id) as c FROM publications").c + 1
            def doiId = sql.firstRow("SELECT COUNT(id) as c FROM dois").c + 1
            
            publications.add (
                title: root.'@title',
                pIssn: root.'@pissn',
                eIssn: root.'@eissn',
                type: root.'@pubType',
                id: publicationId
            )
            
            dois.add(
                doi: root.doi_record.doi.text(),
                articleTitle: root.doi_record.article_title.text(),
                owner: root.doi_record[0].'@owner',
                citationId: root.doi_record[0].'@citationid',
                dateStamp: root.doi_record[0].'@datestamp',
                fileDate: root.'@filedate',
                volume: root.doi_record.volume.text(),
                issue: root.doi_record.issue.text(),
                year: root.doi_record.publication_date.year.text(),
                month: root.doi_record.publication_date.month.text(),
                day: root.doi_record.publication_date.day.text(),
                firstPage: root.doi_record.first_page.text(),
                lastPage: root.doi_record.last_page.text(),
                dumpFilename: dumpFile.getName(),
                publicationId: publicationId,
                id: doiId
            )
            
            root.doi_record.url.collect {
                urls.add(
                    uri: it.text(),
                    host: "", // split uri
                    doiId: doiId
                )
            }
            
            root.doi_record.contributors.collect {
                authors.add(
                    surname: it.surname.text(),
                    givenName: it.given_name.text(),
                    sequence: it.'@sequence',
                    contributorRole: it.'@contributor_role',
                    doiId: doiId
                )
            }
            
            root.publisher.collect {
                def publisherId = sql.firstRow("SELECT COUNT(id) as c FROM publishers").c + 1
                
                publishers.add(
                    name: it.publisher_name.text(),
                    location: it.publisher_location.text(),
                    id: publisherId
                )
                
                publishersDois.add(
                    doiId: doiId,
                    publisherId: publisherId
                )
                
                publishersPublications.add(
                    publicationId: publicationId,
                    publisherId: publisherId
                )
            }
        }
    }
    
}
