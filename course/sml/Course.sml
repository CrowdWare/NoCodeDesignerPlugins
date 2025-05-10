ElementDefinition {
    name: "Course"

    AllowedRoot { name: "App"}
        
    Property {
        name: "lang"
        type: "String"
        default: "en"
        description: "The language of the course."
    }
   
    Property {
        name: "creator"
        type: "String"
        default: ""
        description: "The creator/author of the course."
    }

    Property {
        name: "title"
        type: "String"
        default: ""
        description: "The title of the course."
    }
   
    Property {
        name: "creatorLink"
        type: "String"
        default: ""
        description: "An url to a website that points to the creator."
    }
   
    Property {
        name: "bookLink"
        type: "String"
        default: ""
        description: "An url to a website that points to the course."
    }
 
    Property {
        name: "license"
        type: "String"
        default: ""
         description: "The name of the license the course is published under."
     }
    
    Property {
        name: "licenseLink"
        type: "String"
        default: ""
        description: "An url to a website that points to the license."
    }
}