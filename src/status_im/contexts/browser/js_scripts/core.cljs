(ns status-im.contexts.browser.js-scripts.core
  (:require [shadow.resource :as rc]
            [utils.transforms :as transforms]))

(defn add-global-var
  [script var-name value]
  (-> script
      (str "window.__" var-name "__ = " (transforms/clj->json value) ";")))

(defn add-script
  [script more-script]
  (-> script
      (str more-script)))

;; NOTE: using `rc/inline` instead of `slurp`, so that changes to the js files trigger a recompilation
;; and are included in the bundle. With `slurp`, you'd have to either re-run `shadow-cljs` or
;; re-evaluate the slurp in the REPL.
(def web3-provider (rc/inline "./web3_provider.js"))
(def freeze-website (rc/inline "./freeze_website.js"))
(def unfreeze-website (rc/inline "./unfreeze_website.js"))
(def website-metadata (rc/inline "./website_metadata.js"))
