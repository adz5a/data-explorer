# data-explorer

Simplistic "data-explorer" made with ClojureScript and Reagent.

Let's you load JSON/EDN, see it and edit it.


### Run the project

The project uses leiningen 2.8.1. To make it work with Vim use the following
commands:

```
$ lein repl
=> (require 'figwheel.main.api)
=> (figwheel.main.api/start {:mode :serve} "dev")
=> (figwheel.main.api/cljs-repl "dev")
```

And within a ClojureScript buffer run
```
:Piggieback (figwheel.main.api/repl-env "dev")
```
