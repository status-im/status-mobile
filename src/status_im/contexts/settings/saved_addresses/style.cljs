(ns status-im.contexts.settings.saved-addresses.style)

(def title-container
  {:padding-horizontal 20
   :margin-top         12})

(defn page-wrapper
  [inset-top]
  {:padding-top inset-top
   :flex        1})

(def empty-container-style
  {:justify-content :center
   :flex            1})
