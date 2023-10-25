(ns quo.components.wallet.address-text.view
  (:require [quo.components.markdown.text :as text]
            [quo.foundations.colors :as colors]))


(defn- view-internal
  [{:keys [networks address theme blur?]}]
  [text/text (map (fn [network]
                    ^{:key (str network)}
                    [text/text
                     {:size  :paragraph-2
                      :style {:color (colors/resolve-color network theme)}}
                     (str (subs (name network) 0 3) ":")])
                  networks)
   [text/text
    {:size  :paragraph-2
     :weight :monospace
     :style {:color (if blur?
                      colors/white-opa-40
                      (colors/theme-colors colors/neutral-50 colors/neutral-40 theme))}}
    address]])

(def view (quo.theme/with-theme view-internal))
