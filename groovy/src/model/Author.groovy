package model

class Author {
    static belongsTo = Doi
    
    Doi doi
    
    String surname
    String givenName
    String sequence
    String contributorRole
}
