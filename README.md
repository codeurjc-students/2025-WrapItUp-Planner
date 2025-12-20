# WrapItUp Planner

WrapItUp Planner is an All-In-One collaborative web platform designed to help students manage their academic journey more efficiently. The application combines study organization, knowledge sharing, and AI-powered tools into a single platform. Students can create and share notes, collaborate through comments, and organize their academic life effectively.

## Version 0.1

Version 0.1 of WrapItUp Planner focuses on the note-taking and summary aspects of the application. Users can now create their own accounts, log in, and edit their profiles. The platform enables users to create, edit, delete, and view notes, which can be set as either public or private. Private note owners can share their content with specific users, allowing them to view and comment. Administrator functionality includes access to all notes and the ability to delete notes and comments when necessary. Users have a dedicated notes page where they can browse their content organized by categories and view notes that have been shared with them.

### Screenshots

[![Landing Page](images/v0.1/landing.png)](images/v0.1/landing.png)
*Landing page with application introduction*

[![About Us](images/v0.1/about-us.png)](images/v0.1/about-us.png)
*About Us page with platform information*

[![My Notes](images/v0.1/my-notes.png)](images/v0.1/my-notes.png)
*Personal notes organized by category*

[![Note Details](images/v0.1/note-details.png)](images/v0.1/note-details.png)
*Note details page with comments section*

[![Profile](images/v0.1/profile.png)](images/v0.1/profile.png)
*User profile page*

### Demo Video

Below is a demo video showcasing the application:

[![Demo Video](https://img.youtube.com/vi/zgzQLEhDZpY/0.jpg)](https://youtu.be/zgzQLEhDZpY)

---

**Features demonstrated by user type:**
- **Unregistered Users**: View public notes, and landing page
- **Registered Users**: Create accounts, manage notes, share content, leave comments, visit and edit their profile
- **Administrators**: Moderate content, delete notes and comments

### Current Development Status

The application is currently in development and will be continuously improved as further versions are released.

## Future Versions

The next release of WrapItUp Planner will focus on the calendar component of the application, enabling users to organize their tasks, create events, and maintain daily to-do lists to structure their day-to-day activities. The final release will add AI-powered functionalities to further enhance the notes component, including automatic generation of overviews, summaries, and interactive quizzes from uploaded study materials.

### Functionalities (Version 0.1)

[![Landing Page](images/v0.1/landing.png)](images/v0.1/landing.png)
*Landing page with application introduction*

[![About Us](images/v0.1/about-us.png)](images/v0.1/about-us.png)
*About Us page with platform information and goals*

[![Login](images/v0.1/login.png)](images/v0.1/login.png)
*Users will be able to login to their own account to have access to their personal notes and user's functionalities*

[![Register](images/v0.1/register.png)](images/v0.1/register.png)
*Users will be able to create their own account to access to their own page*

[![Profile](images/v0.1/profile.png)](images/v0.1/profile.png)
*Users and Admins can visit their own profile page and edit their personal data, such as their visible name, email and profile photo. They may also access the notes page (in the case of being a regular user)*

[![My Notes](images/v0.1/my-notes.png)](images/v0.1/my-notes.png)
*Registered Users will be able to see all their notes in an organized format divided by categories, they will also be able to see notes that were shared with them*

[![Create Note](images/v0.1/create-note.png)](images/v0.1/create-note.png)
*Registered users will be able to create their own personal notes as they see fit. They can also decide the visibility of said note.*

[![Note Details](images/v0.1/note-details.png)](images/v0.1/note-details.png)
*Users will be able to see previously created notes and will have access to an available comment section to communicate with other users. The owners of these notes will be able to share the notes with other users which will give those users access to another user's private notes*

[![Error Page](images/v0.1/error-page.png)](images/v0.1/error-page.png)
*Error page for handling server errors*

### Detailed Functionalities

**Basic Functionality -  IMPLEMENTED**

| User Type           | Features |
|--------------------|---------|
| Unregistered Users  | - Register and log in to the platform.<br>- View notes if they have access to the link and the notes are not set as private.|
| Registered Users    | - Access a personal profile page.<br>- Access to their personal list of notes.<br>- View notes, whether their own or another user's.<br>- Leave comments on notes they have access to.<br>- Communicate with other authorized users through a comment section associated with each note. |
| Administrators      | - Full access to all user profiles and uploaded notes.<br>- Ability to delete notes and comments. |

### Planned Features

**Intermediate Functionality - NEXT RELEASE (Version 0.2)**

| User Type           | Features |
|--------------------|---------|
| Registered Users    | - Use an interactive calendar to create events spanning one or multiple days.<br>- Manage daily tasks in the form of a to-do list.<br>- View the calendar in both monthly and daily formats, with a modal window appearing upon selecting a specific day in the monthly view.<br>- Report inappropriate comments made by other users. |
| Administrators      | - Review user-reported comments in a ticket-based format. <br>- Ability to ban a user from accessing the platform. |

**Advanced Functionality - PLANNED (Version 1.0)**

| User Type           | Features |
|--------------------|---------|
| Registered Users    | - Upload notes to the platform, which will be processed using artificial intelligence to generate an overview, a summary, and multiple-choice questions in JSON format.<br>- Access interactive quizzes derived from the JSON output for effective study sessions.<br>- View a heatmap in their profile displaying the number of daily tasks assigned during the current month.<br>- View a line chart showing performance progression upon completing a quiz. |

## How to Run the Application

### Prerequisites

Before running the application, ensure you have Docker installed on your system:

- **Windows and Mac**: Install [Docker Desktop](https://www.docker.com/products/docker-desktop/)
- **Linux**: Install [Docker Engine](https://docs.docker.com/engine/install/) and [Docker Compose](https://docs.docker.com/compose/install/)

### Running the Application

To run the application on your local machine, execute the following commands in your terminal:

```bash
docker pull arturox2500/wrapitup_planner:0.1
docker pull arturox2500/wrapitup_planner-compose:0.1
docker create --name temp-compose arturox2500/wrapitup_planner-compose:0.1 cmd.exe
docker cp temp-compose:/docker-compose.yml ./docker-compose.yml
docker cp temp-compose:/.env ./.env
docker rm temp-compose
docker compose up -d
```

### Accessing the Application

Once the containers are running, you can access the application at:

**Web Application**: [https://localhost:443](https://localhost:443)

### Sample User Credentials

The application comes with pre-loaded sample data for testing purposes. You can log in with any of the following accounts:

| Username | Email | Password | Role |
|----------|-------|----------|------|
| genericUser | genericUser@example.com | 12345678 | User |
| secondUser | secondUser@example.com | 12345678 | User |
| admin | admin@example.com | 12345678 | Administrator |

### Sample Data Overview

The application is initialized with sample data to demonstrate its functionality:

**Users**:
- **genericUser**: Regular user with 12 notes across different categories and a profile picture
- **secondUser**: Regular user with 6 notes and a profile picture
- **admin**: Administrator account with full access to all notes and moderation capabilities, they have to access notes through an url in this phase

**Notes**:
- 18 total notes covering various subjects (Mathematics, Science, History, Art, Languages, and Others)
- Notes are categorized by topic (e.g., "Pythagorean Theorem" under MATHS, "Photosynthesis" under SCIENCE)
- Mix of public and private notes
- Some notes are shared between users (e.g., genericUser's "Pythagorean Theorem" is shared with secondUser)

**Comments**:
- Comments from both genericUser and secondUser showing interaction between users


## Documentation

- [Project Beginning](docs/readme/Project-Beginning.md) – Complete project overview with objectives, methodology, and planned features
- [Introduction](docs/readme/Introduction.md) – Overview of the development guide
- [Technologies](docs/readme/Technologies.md) – Languages, libraries, and services used
- [Tools](docs/readme/Tools.md) – IDEs and auxiliary development tools
- [Architecture](docs/readme/Architecture.md) – System architecture and components
- [Quality Control](docs/readme/Quality-control.md) – Testing and code analysis
- [Development Process](docs/readme/Development-process.md) – Git workflow and CI/CD
- [Deployment](docs/readme/Deployment.md) – Packaging, distribution, and Docker deployment
- [Code Execution and Editing](docs/readme/Code-Execution-And-Editing.md) – Setup and run instructions

## Project Information

This application is developed as part of the Final Degree Project (TFG) for the Software Engineering degree at the School of Computer Engineering (ETSII) of Universidad Rey Juan Carlos (URJC). The project is carried out by Arturo Enrique Gutierrez Mirandona under the supervision of Micael Gallego.

### Progress Tracking

Project progress is documented through a [Medium blog](https://medium.com/@gutierrezarturox) with development announcements and GitHub Projects for task management.

---

*For detailed information about the complete project scope, planned features, methodology, and all future functionalities, see the [Project Beginning](docs/readme/Project-Beginning.md) documentation.*
