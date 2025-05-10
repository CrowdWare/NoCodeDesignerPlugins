App {
    name: "DemoWorkshop"
    id: "com.sample.app.DemoWorkshop"
    version: "1.0.0"
    description: "This has to be filled."
    author: "Adam Art Ananda"


    Course {
        lang: "de"
        title: "Demo Workshop"
        creator: "Adam Art Ananda"

        Topic {
            label: "Topic 1"

            Lecture {
                label: "Lecture 1"
                src: "lecture_1.sml"
            }

            Lecture {
                label: "Lecture 2"
                src: "lecture_2.sml"
            }
        }

        Topic {
            label: "Topic 2"

            Lecture {
                label: "Lecture 3"
                src: "lecture_3.sml"
            }

            Lecture {
                label: "Lecture 4"
                src: "lecture_4.sml"
            }
        }

        Topic {
            label: "Topic 3"

            Lecture {
                label: "Lecture 3"
                src: "lecture_3.sml"
            }

            Lecture {
                label: "Lecture 4"
                src: "lecture_4.sml"
            }
        }
    }

    Theme {
    primary: "#FFB951"
    onPrimary: "#452B00"
    primaryContainer: "#633F00"
    onPrimaryContainer: "#FFDDB3"
    secondary: "#DDC2A1"
    onSecondary: "#3E2D16"
    secondaryContainer: "#56442A"
    onSecondaryContainer: "#FBDEBC"
    tertiary: "#B8CEA1"
    onTertiary: "#243515"
    tertiaryContainer: "#3A4C2A"
    onTertiaryContainer: "#D4EABB"
    error: "#FFB4AB"
    errorContainer: "#93000A"
    onError: "#690005"
    onErrorContainer: "#FFDAD6"
    background: "#1F1B16"
    onBackground: "#EAE1D9"
    surface: "#1F1B16"
    onSurface: "#EAE1D9"
    surfaceVariant: "#4F4539"
    onSurfaceVariant: "#D3C4B4"
    outline: "#9C8F80"
    inverseOnSurface: "#1F1B16"
    inverseSurface: "#EAE1D9"
    inversePrimary: "#825500"
    surfaceTint: "#FFB951"
    outlineVariant: "#4F4539"
    scrim: "#000000"
  }
}
