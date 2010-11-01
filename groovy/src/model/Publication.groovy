package model

class Publication {
    static hasMany = [dois:Doi, publishers:Publisher]
    
    String title
    String pIssn
    String eIssn
    String publicationType
}
