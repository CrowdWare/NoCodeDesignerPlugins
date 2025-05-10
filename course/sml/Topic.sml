ElementDefinition {
    name: "Topic"

    AllowedRoot { name: "Course"}

    Property {
        name: "label"
        type: "String"
        default: ""
        description: "The name of the topic."
    }
}