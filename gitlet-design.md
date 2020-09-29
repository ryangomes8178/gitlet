# Gitlet Design Document

**Name**: Ryan Gomes

## Classes and Data Structures
### Main
This class will be able to run the program with a user input and output a result. This class executes certain commands from other classes that the user has called on the command line. Additionally, the main class creates a version control system that helps the user save snapshots of your files at different points in time. That way, if the user messes them up later, the user can return to earlier versions.

**Fields**

1. args: a set of arguments that represents the input that the user has put into the program (will include command, operand, object, and more depending on how the user wants to manipulate the Gitlet object)
2. return: creates a gitlet object



### Gitlet Exception
This class is used for error handling, where the program can indicate a Gitlet error due to an incorrect user input, with an appropriate error message. This class is also used for debugging to ensure that specific user inputs will not cause the program to error.

**Fields**

1. private instance variables: objects that are inherited from the super class, Runtime Exception, which include error exceptions, java exceptions, and error messages
2. message: displays an appropriate message to the user for the corresponding error

### Gitlet
This class is used to create a functional Gitlet object that can commit, check out, log, maintaining branches, merging, etc. This class is the operational class that manages all the files of the user and keeps them up date.

**Fields**

1. staging area: an object that stores the active files to a repository (serves as an accessible directory for the user to push or restore files)
2. commit tree: an object, tree data structure, that stores the committed files into a staging area, where it can be pushed 
3. head: an object that represents the most recent, or current, commit file that the user is manipulating

### Utils
This class is used to retrieve certain methods that can be helpful to create and alter objects in different classes.

**Fields**

1. accessing SHA-1 hash values: sha1
2. deleting file and file contents: restrictedDelete
3. reading and writing file contents: readContents, readContentsAsString, writeContents, readObject, writeObject
4. accessing, creating, and removing directories: plainFilenamesIn
5. reporting errors with appropriate messages: error, message
6. serialization utilities: serialize
7. other file utilities: join


## Algorithms
These algorithms are specific methods from different classes that will run on the command line as a result of the user input.
1. commit: method that saves the contents of entire directories of files into a staging area where the user can access
2. checking out: method that restores a version of one or more files or entire commits to a previous version
3. log: method that displays the history of all the backups to the user
4. merge: method that overrides certain changes made in one branch into another depending on the chosen branch by the user
5. init: method that initializes a gitlet object that has accessible methods 
6. status: method that displays the current status of your tree (in other words, it checks whether or not your commit tree is up to date)
7. find: will take input of a commit message and find the corresponding file with the correct commit ID


## Persistence
Need to record the state of the program or files after the following calls:
1. commit
2. merge
3. check out

