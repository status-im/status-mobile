(ns quo.components.wallet.account-permissions.view
  (:require [clojure.string :as string]
            [quo.components.avatars.account-avatar.view :as account-avatar]
            [quo.components.dividers.divider-line.view :as divider-line]
            [quo.components.icon :as icons]
            [quo.components.markdown.text :as text]
            [quo.components.selectors.selectors.view :as selectors]
            [quo.components.wallet.account-permissions.schema :as component-schema]
            [quo.components.wallet.account-permissions.style :as style]
            [quo.components.wallet.address-text.view :as address-text]
            [quo.components.wallet.required-tokens.view :as required-tokens]
            [quo.foundations.colors :as colors]
            [quo.theme :as quo.theme]
            [react-native.core :as rn]
            [schema.core :as schema]
            [utils.i18n :as i18n]))

(defn- token-details-section
  [tokens]
  (when tokens
    [:<>
     [divider-line/view]
     [rn/view {:style style/row2-content}

      (if (empty? tokens)
        [text/text
         {:size  :paragraph-2
          :style style/no-relevant-tokens}
         (i18n/label :t/no-relevant-tokens)]

        (let [token-length (dec (count tokens))]
          (map-indexed
           (fn [idx
                {:keys              [type token amount collectible-name collectible-img-src
                                     token-img-src]
                 collectible-symbol :symbol}]
             ^{:key idx}
             [required-tokens/view
              {:container-style     style/token-and-text
               :type                type
               :amount              amount
               :token               token
               :token-img-src       token-img-src
               :collectible-img-src collectible-img-src
               :collectible-name    (if (string/blank? collectible-name)
                                      collectible-symbol
                                      collectible-name)
               :divider?            (not= token-length idx)}])
           tokens)))]]))

(defn- view-internal
  [{:keys
    [checked? disabled? on-change token-details keycard? container-style customization-color]
    {:keys
     [name address emoji]
     :as account} :account
    :or {customization-color :blue}}]
  (let [theme (quo.theme/use-theme)]
    [rn/view
     {:style               (merge (style/container theme) container-style)
      :accessibility-label :wallet-account-permissions}
     [rn/view {:style style/row1}
      [account-avatar/view
       {:size                32
        :emoji               emoji
        :customization-color (:customization-color account)}]
      [rn/view {:style style/account-details}
       [rn/view {:style style/name-and-keycard}
        [text/text
         {:size   :paragraph-1
          :weight :semi-bold} name]
        (when keycard?
          [icons/icon :i/keycard-card
           {:accessibility-label :wallet-account-permissions-keycard
            :size                20
            :color               (colors/theme-colors colors/neutral-50 colors/neutral-40 theme)}])]
       [address-text/view
        {:address address
         :format  :short}]]
      [selectors/view
       {:type                :checkbox
        :checked?            checked?
        :customization-color customization-color
        :disabled?           disabled?
        :on-change           on-change}]]

     [token-details-section token-details]]))

(def view (schema/instrument #'view-internal component-schema/?schema))
