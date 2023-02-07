# Note

This is currently a bespoke plugin for a singularity group unity csharp project.
Don't expect it to work please.

# IdeaIdle

<!-- Plugin description -->

SingularityGroup internal intellij plugin.

> **performance** it will add ~0.5s to your rider startup time.
> No other performance penalty is known.

## Installation

- clone this [repo](https://gitlab.botogames.com/singularitygroup/ideaidle)
(your are likely aleady here reading the Readme)

```
cd
mkdir -p repos
cd repos
git clone git@gitlab.botogames.com:singularitygroup/ideaidle.git
cd ideaidle
./install-ideaidlerc.sh
```
- do  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>

![](https://sg-support-bot-public.s3.amazonaws.com/ideaidle/plugin-from-disk.png)

- select `path-to-ideaidle/bin/IdeaIdle-0.0.1.zip` (inside the repo
  you just cloned)

- Restart rider ( it should ask you to )

- when you start typing in the idlegame project there should be a rider process at the bottom saying "ideaidle: Indexing components" this will take 1 minute

- the 2 entry point actions to the plugin are `CompsearchActions` and
  `IdleActions` (they show up in the [keymap setting](https://imgur.com/a/1FGqmA2))

you can search for  `CompsearchActions` and `IdleActions` in the keybinding settings
they also show up when you search for "Actions..."


> *info* when windows asks you for permission that the rider program
> opens a network port, say yes.
> Else your component completions won't update while you type new components.

### Fix the path
The install creates a folder in your home `~/.ideaidle.d`.
Make sure this path at the bottom of the `~/ideaidle.d/config.edn` is correct

```
 :config/ideaidle-home "path/to/your/ideaidle"
 ```

> use forward slashes "/" as path seperators


## Features

check [Readme](https://gitlab.botogames.com/singularitygroup/ideaidle)


### Component completions

[showcase1](https://i.imgur.com/fjdqw5C.png)

- when you start typing in IdleGame the first time the plugin will
  start indexing components
  (it will say on bottom right "ideaidle: Indexing components ")
  this takes a minute or something

> this can be triggered later again with `ctrl l, l` (IdleActions) -> "Collect Components"

- type `.state.get@` and complete UniqueComponents
- type `.is@`  and complete only flags and so forth
- type `<@` to complete any kind of Component

- completions are defined for you in `~/.ideaidle.d/config.edn` under the
  key `:completion/table`
- opening this file in rider or vs code is fine. Emacs even better.
- start hacking on it, reload with IdleActions -> "Load ideaidle config"
- run `install-ideaidlerc.sh` if you ever want to go back to defaults

- do not worry about performance unless you put 10k items there

> Nothing in config.edn ever needs a restart, just "load"

> *info* While you type there is a roslyn analyzer that collects the
> components as you type. For this to work you need roslyn analyzers setup.
> IdleGame/.rider-analzyer.conf should be present (and you Project is
> in the list), IdleGame/rider-analyzer-setup.bat to generate it

#### default prefixes

quick start if you don't want to look at the config file:

```
.state.get@
.state.add@
.state.has@
.state.set@
.state.is@
.set@
.is@
.add@
.get@
 GetEntityWith@
 gew@
 GetEntitiesWith@
 geww@
 react@
 <@
```


### Component Searches + custom searches

- make a regex search for component methods
- press control + L, then u (for usages)

- searches are exposed in the config
- check the config for examples

``` clojure
  #:action
  {
   ;; id that works with ideavim
   :id "ideaidle.example.customsearch"
   :display-text "My Search AddReactEach"
   :search-fmt "AddReactEach.+Matcher.+%s"}
```

### actions

- IdleActions action menu bound by default to `ctrl L, L` (control + l
  followed another l)

- "Load ideaidle config" reload your `~/ideaidle.d/config.edn` after
  making changes

- "Collect all components" : starts the component indexing job for the
  current solution, happens 1 time auto after you opened the solution

- "Check component completions status" : prints how many components
  are indexed


### ideavim

If you use ideavim the actions ids are

	com.github.benjaminasdf.idlelib.compsearchActions.CompsearchActions

	com.github.benjaminasdf.idlelib.idleactions.IdleActions

you can do

	:action com.github.benjaminasdf.idlelib.idleactions.IdleActions

and bind it like other editor actions (presumably in .ideavimrc)

The search actions themself also have action ids

eg.

	ideaidle.compsearch.get.value

(see `~/ideaidle.d/config.edn`)

<!-- Plugin description end -->

# Dev

## Nrepl
See  `src/main/elisp/devel.el`. Start an nrepl server when
IDEAIDLE_REPL_PORT is set.

