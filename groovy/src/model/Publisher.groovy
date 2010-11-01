package model

class Publisher {
    static hasMany = [dois:Doi, publications:Publication]
    
    String name
    String location
}
