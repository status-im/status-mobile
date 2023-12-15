(ns quo.components.wallet.account-permissions.view
  (:require [quo.components.avatars.account-avatar.view :as account-avatar]
            [quo.components.dividers.divider-line.view :as divider-line]
            [quo.components.icon :as icons]
            [quo.components.markdown.text :as text]
            [quo.components.selectors.selectors.view :as selectors]
            [quo.components.wallet.account-permissions.style :as style]
            [quo.components.wallet.address-text.view :as address-text]
            [quo.components.wallet.required-tokens.view :as required-tokens]
            [quo.foundations.colors :as colors]
            [quo.theme :as quo.theme]
            [react-native.core :as rn]
            [schema.core :as schema]
            [utils.i18n :as i18n]))

(def ?schema
  [:=>
   [:catn
    [:props
     [:map {:closed true}
      [:account
       [:map {:closed true}
        [:name [:maybe :string]]
        [:address [:maybe :string]]
        [:emoji [:maybe :string]]
        [:customization-color [:maybe [:or :string :keyword]]]]]
      [:token-details {:optional true} [:maybe [:vector required-tokens/?schema]]]
      [:keycard? {:optional true} [:maybe :boolean]]
      [:checked? {:optional true} [:maybe :boolean]]
      [:disabled? {:optional true} [:maybe :boolean]]
      [:on-change {:optional true} [:maybe fn?]]
      [:theme :schema.common/theme]]]]
   :any])

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
          (map-indexed (fn [idx {:keys [token amount]}]
                         ^{:key idx}
                         [required-tokens/view
                          {:container-style style/token-and-text
                           :type            :token
                           :amount          amount
                           :token           token
                           :divider?        (not= token-length idx)}])
                       tokens)))]]))

(defn- view-internal
  [{:keys
    [checked? disabled? on-change token-details keycard? theme]
    {:keys
     [name address emoji customization-color]} :account}]
  [rn/view
   {:style               (style/container theme)
    :accessibility-label :wallet-account-permissions}
   [rn/view {:style style/row1}
    [account-avatar/view
     {:size                32
      :emoji               emoji
      :customization-color customization-color}]
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
     {:type      :checkbox
      :checked?  checked?
      :disabled? disabled?
      :on-change on-change}]]

   [token-details-section token-details]])

(def view
  (quo.theme/with-theme
   (schema/instrument #'view-internal ?schema)))
