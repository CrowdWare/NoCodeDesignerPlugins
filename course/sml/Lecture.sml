ElementDefinition {
    name: "Lecture"

    AllowedRoot { name: "Topic"}
    
    Property {
        name: "label"
        type: "String"
        default: ""
        description: "The name of the lecture."
    }

    Property {
        name: "src"
        type: "String"
        default: ""
        description: "The filename of the lecture. Example: lecture_1.sml"
    }
}