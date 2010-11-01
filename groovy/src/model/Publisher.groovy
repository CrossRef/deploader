package model

class Publisher {
    static hasMany = [dois:Doi, publications:Publication]
    static belongsTo = Publication
    
    String name
    String location
}
