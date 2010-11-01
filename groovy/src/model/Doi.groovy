package model

class Doi {
    static hasMany = [authors:Author, urls:Url, publishers:Publisher]
    static belongsTo = Publication
    
    Publication publication
    
    String xml
    String articleTitle
    String owner
    String citationId
    String dateStamp
    String fileDate
    String volume
    String issue
    String year
    String month
    String day
    String firstPage
    String lastPage
    String dumpFilename
}
