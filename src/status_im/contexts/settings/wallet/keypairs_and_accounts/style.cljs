(ns status-im.contexts.settings.wallet.keypairs-and-accounts.style)

(def title-container
  {:padding-horizontal 20
   :margin-vertical    12})

(defn page-wrapper
  [top-inset]
  {:padding-top top-inset
   :flex        1})

(defn list-container
  [bottom-inset]
  {:padding-bottom bottom-inset})

(def keypair-container-style
  {:margin-horizontal 20
   :margin-vertical   8})
