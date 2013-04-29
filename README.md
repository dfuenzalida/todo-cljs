# todo-cljs

A ClojureScript port of the JavaScript TODOs app from http://todomvc.com/

Includes the template and CSS by Sindre Sorhus

![screenshot](http://4.bp.blogspot.com/-s3FkdftKDzo/US_LtUoKnGI/AAAAAAAAEKw/-WupdgyHRUs/s1600/todos-cljs.png)

## Usage

```
$ lein deps
$ lein cljsbuild once
$ cd resources/public
$ python -m SimpleHTTPServer 8888
```

then open `http://0.0.0.0:8888/` in your browser to visit the application.

## License

Copyright © 2013 Denis Fuenzalida

Distributed under the Eclipse Public License, the same as Clojure.
