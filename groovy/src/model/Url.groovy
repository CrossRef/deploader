package model

class Url {
    static belongsTo = Doi
    Doi doi
    
    String uri
    String host
}
