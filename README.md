# bbq

A small HTTP client built on [babashka](https://github.com/babashka/babashka). Templates are plain Clojure forms, so requests live in your repo alongside the rest of your code, version naturally, and can use the full power of babashka!

![demo](assets/demo.gif?v=2)

## Installation

Requires [`bb`](https://github.com/babashka/babashka#installation) (`brew install borkdude/brew/babashka`) and [`bbin`](https://github.com/babashka/bbin) (`brew install babashka/brew/bbin`).

```sh
bbin install io.github.gmartsenkov/bbq
```

Or from a local checkout:

```sh
bbin install /path/to/bbq
```

`bbq` is now on your `$PATH`.

**Shell completion** (optional — type `bbq <TAB>` to see template names). The repo ships `completions/bbq.zsh`, `bbq.bash`, and `bbq.fish`; source whichever fits your shell:

```sh
# zsh
echo "source <path-to-bbq>/completions/bbq.zsh" >> ~/.zshrc
exec zsh
```

## Basic usage

Create a `bbq.edn` at the root of your project:

```clojure
{:dirs      ["auth" "users"]
 :variables {:org "acme"}}
```

Create a template anywhere under one of those `:dirs`, e.g. `users/show.clj`:

```clojure
(require '[bbq :refer [bearer v]])

{:title   "Users — Show"
 :method  :GET
 :uri     (str "https://api.example.com/orgs/" (v :org) "/users/" (v :id))
 :headers {"authorization" (bearer "demo-token")}}
```

Then:

```sh
bbq                              # list available templates
bbq users/show id=42             # run with an override
bbq users/show id=42 --as=curl   # render the request as a curl one-liner
bbq users/show id=42 --pager       # open the response body in a viewer (defaults to bat → less)
bbq users/show id=42 --pager=nvim  # …or pin to any command that accepts the body on stdin
bbq --help
```

CLI overrides are `key=value`. They become `:key`-keyed entries in the variable map and shadow whatever's in `bbq.edn`.

## Templating

A template is a `.clj` file that evaluates to a request map. Any keys not consumed by [`babashka.http-client`](https://github.com/babashka/http-client) (like `:title`) are ignored at request time but available for `bbq`'s own use (the listing reads `:title`).

### Variables

- Defined under `:variables` in `bbq.edn`, keyword-keyed (`{:org "acme"}`).
- CLI overrides (`id=42`) win over config values.
- Read inside templates with `(v :key)` — throws if unset, `(v :key default)` for a fallback.

### Helpers

`(:require [bbq :refer [...]])` pulls in:

| Helper | What it does |
|--|--|
| `(v :key)` / `(v :key default)` | Look up a variable from the merged config + override map. |
| `(env "NAME")` | Read an environment variable. Throws if unset. |
| `(bearer token)` | → `"Bearer <token>"`. |
| `(basic user pass)` | → `"Basic <base64(user:pass)>"`. |
| `(base64-encode s)` | Base64-encode a string (UTF-8). |
| `(json-encode x)` | Serialize a Clojure value as JSON. |
| `(json-request "tmpl/name")` | Run another template, parse the response body as JSON, return it as a navigable map. |

Because templates are just Clojure forms, you can also `(require '[clojure.string :as str])`, pull in a Clojars dep at runtime with `(babashka.deps/add-deps '{:deps {...}})`, or chain requests using `let`:

```clojure
(require '[bbq :refer [bearer json-request v]])

(let [login (json-request "auth/login")
      token (-> login :data :token)]
  {:title   "Users — Show (authed)"
   :method  :GET
   :uri     (str "https://api.example.com/users/" (v :id))
   :headers {"authorization" (bearer token)}})
```

### Request map shape

| Key | Required | Notes |
|--|--|--|
| `:method` | yes | `:GET`, `:POST`, `:PUT`, `:DELETE`, etc. |
| `:uri` | yes | Full URL. May include a query string. |
| `:query-params` | no | Map of `:key value` pairs appended to `:uri`. Values are URL-encoded; sequences expand to repeated keys. |
| `:headers` | no | Map of string keys → string values. |
| `:body` | no | Usually a JSON-encoded string. |
| `:title` | no | Optional short title shown in `bbq` (template listing). |

See `examples/` in the repo for working samples: a simple GET, a chained-auth template using `json-request`, and a multi-form template pulling in `clojure.set`.

## Development

```sh
bb test                  # run the test suite (from examples/ for cwd-relative lookups)
clj-kondo --lint src     # static analysis
vhs demo.tape            # regenerate assets/demo.gif
```
