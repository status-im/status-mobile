# Status Quo Components
All components in **Quo** should be independent of the app state. They should be pure,
and easy to reason about. This is required to make the library independent and
easily pulled off as a separate repository when needed.
Do avoid high coupling and direct use of internal styling, the components should be
exported via namespace `quo.core` and used by status app only from here. This will
allow a more flexible way to update components without possible breakages into the
app style.

**Quo** components should and not have any dependency on the status app, this
will avoid circular dependency and also benefit the independence of the components.

All components are stored inside `components` namespaces. They are stateless and do
not dispatch and subscribe to re-frame database. All state should be passed by props
and all events can be passed as functions. Avoiding direct connection with re-frame
will allow components to grow and be reused in different places without the
conditionals hell.

All style system constants are stored inside `design-system` namespaces. They are used
to build components and can be directly required by the status app. Avoid
duplication of these vars and do not use them in code directly as a value.

For each component introduced, add previews of all possible states.

Do not introduce components for slightly modified existing components, if they are
not a part of the design system. In case they are required in one place in the app,
use style override.

# Code style
Ensure that your changes match the style of the rest of the code.
This library uses Clojure Community code style [The Clojure Style Guide](https://github.com/bbatsov/clojure-style-guide)
To ensure consistency run [clj-kondo linter](https://github.com/borkdude/clj-kondo)

# Best practices

- Desing components atomically and compose them into bigger components.
- Do not export individual atoms, only components. This way we can limit design
system to be used in too many way which can creating disjointed experiences.
- Avoid external margins for atom components, it can be added on the wrapper
where they are used but can't be removed without overriding.
[Max Stoiber article on margins](https://mxstbr.com/thoughts/margin)
- Design reusable components into [Layout Isolated Components](https://visly.app/blog/layout-isolated-components)
(Article more relates to web, but ideas fits also to mobile dev**
- Explicit is better than implicit, do not rely on platform default, if you expect
a specific value, then override it

**TBD:**
- Components documentation
- Check props using spec in pre conditions.
