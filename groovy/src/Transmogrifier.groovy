import model.Author;
import model.Doi;
import model.Publication;
import model.Publisher;
import model.Url;


class Transmogrifier {
    
    def transmogrify(root, dumpFile) {
        def doi = new Doi()
        doi.doi = root.publication.doi_record.doi.text()
        doi.articleTitle = root.publication.doi_record.article_title.text()
        doi.owner = root.publication.doi_record.'@owner'
        doi.citationId = root.publication.doi_record.'@citationid'
        doi.dateStamp = root.publication.doi_record.'@datestamp'
        doi.fileDate = root.publication.'@filedate'
        doi.volume = root.publication.doi_record.volume.text()
        doi.issue = root.publication.doi_record.issue.text()
        doi.year = root.publication.doi_record.publication_date.year.text()
        doi.month = root.publication.doi_record.publication_date.month.text()
        doi.day = root.publication.doi_record.publication_date.day.text()
        doi.firstPage = root.publication.doi_record.first_page.text()
        doi.lastPage = root.publication.doi_record.last_page.text()
        doi.dumpFilename = dumpFile.getName()
        doi.save()
        
        def publication = new Publication()
        publication.title = root.publication.'@title'
        publication.pIssn = root.publication.'@pissn'
        publication.eIssn = root.publication.'@eissn'
        publication.type = root.publication.'@pubType'
        publication.addToDois(doi)
        publication.save()
        
        root.publication.doi_record.url.collect {
            def url = new Url()
            url.uri = it.text()
            url.host = it.text() // split uri
            uri.doi = doi
            uri.save()
        }
        
        root.publication.doi_record.contributors.collect {
            def author = new Author()
            author.surname = it.surname.text()
            author.givenName = it.given_name.text()
            author.sequence = it.'@sequence'
            author.contributorRole = it.'@contributor_role'
            author.doi = doi
            author.save()
        }
        
        root.publication.publisher.collect {
            def publisher = new Publisher()
            publisher.name = it.publisher_name.text()
            publisher.location = it.publisher_location.text()
            publisher.addToDois(doi)
            publisher.addToPublications(publication)
            publisher.save()
        }
    }
    
}
