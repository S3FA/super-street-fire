# Purpose and Description #

The Utils package is intended to provide all general-purpose functionality and the associated data structures to all other packages/modules in Super Street Fire. The Utils package will contain at the bare minimum the following types of functionality

  * Mathematical functions and data structures
  * Info/Warning/Error/Debug Logging functionality
  * Custom data structures (e.g., thread-safe data structures that have to be specially written for the project)

The Utils package is intended to be highly coupled with all other packages in the system since it's main intention as an independent package is to centralize a lot of code that will be used more than once across the Super Street Fire project.


# Interface #

The Utils package will expose all of its data structures and their public methods as its interface. The module is intended to provide tools to the rest of the modules. As described above, it will expose whatever methods are necessary for providing the general-purpose functionality required by other packages.

# Interactions and Dependencies #

### All packages depend on Utils ###

The Utils package is intended to provide general-use utilities to all of the other software packages in the Super Street Fire project. For example, most packages will need to log their activity in some way, either for debugging purposes or general introspection; the Utils package will contain a logger class for such purposes. Furthermore, any mathematical (trigonomic, algebraic, etc.) functionality and objects (vectors, points, etc.) that may be required by other modules will reside in the Utils package.