(ns quo.components.wallet.required-tokens.view
  (:require [quo.components.markdown.text :as text]
            [quo.components.utilities.token.view :as token]
            [quo.components.wallet.required-tokens.schema :as required-tokens-schema]
            [quo.components.wallet.required-tokens.style :as style]
            quo.context
            [react-native.core :as rn]
            [schema.core :as schema]))

(defn- view-internal
  [{:keys [type amount token token-img-src collectible-img-src collectible-name divider?
           container-style]}]
  (let [theme (quo.context/use-theme)]
    [rn/view
     {:style               (merge style/container container-style)
      :accessibility-label :wallet-required-tokens}
     (case type
       :token       [token/view
                     (assoc (if token-img-src
                              {:image-source token-img-src}
                              {:token token})
                            :size
                            14)]
       :collectible [rn/image
                     {:style  style/collectible-img
                      :source collectible-img-src}]
       nil)
     [text/text
      {:size   :paragraph-2
       :weight :medium
       :style  {:margin-left 4}}
      (case type
        :token       (str amount " " token)
        :collectible (str amount " " collectible-name)
        nil)]
     (when divider?
       [rn/view
        {:style (style/divider theme)}])]))

(def view (schema/instrument #'view-internal required-tokens-schema/?schema))
