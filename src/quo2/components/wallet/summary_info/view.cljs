(ns quo2.components.wallet.summary-info.view
  (:require
    [quo2.components.avatars.user-avatar.view :as user-avatar]
    [quo2.components.markdown.text :as text]
    [quo2.foundations.colors :as colors]
    [quo2.foundations.resources :as resources]
    [quo2.theme :as quo.theme]
    [quo2.components.avatars.account-avatar.view :as account-avatar]
    [react-native.core :as rn]
    [quo2.components.wallet.summary-info.style :as style]))

(defn- network-amount
  [{:keys [network amount divider? theme]}]
  [:<>
   [rn/image
    {:style  {:width  14
              :height 14}
     :source (resources/get-network network)}]
   [text/text
    {:size   :paragraph-2
     :weight :medium
     :style  {:margin-left 4}} amount]
   (when divider?
     [rn/view
      {:style (style/dot-divider theme)}])])

(defn networks
  [values theme]
  (let [{:keys [ethereum optimism arbitrum]} values]
    [rn/view
     {:style               style/networks-container
      :accessibility-label :networks}
     [network-amount
      {:network  :ethereum
       :amount   (str ethereum " ETH")
       :divider? true
       :theme    theme}]
     [network-amount
      {:network  :optimism
       :amount   (str optimism " ETH")
       :divider? true
       :theme    theme}]
     [network-amount
      {:network :arbitrum
       :amount  (str arbitrum " ETH")
       :theme   theme}]]))

(defn- view-internal
  [{:keys [theme type account-props networks? values]}]
  [rn/view
   {:style (style/container networks? theme)}
   [rn/view
    {:style style/info-container}
    (if (= type :status-account)
      [account-avatar/view account-props]
      [user-avatar/user-avatar account-props])
    [rn/view {:style {:margin-left 8}}
     (when (not= type :account) [text/text {:weight :semi-bold} (:name account-props)])
     [rn/view
      {:style {:flex-direction :row
               :align-items    :center}}
      (when (= type :user)
        [:<>
         [rn/view {:style {:margin-right 4}} [account-avatar/view (:status-account account-props)]]
         [text/text
          {:size  :paragraph-2
           :style {:color (colors/theme-colors colors/neutral-50 colors/neutral-40 theme)}}
          (get-in account-props [:status-account :name])]
         [rn/view
          {:style (style/dot-divider theme)}]])
      [text/text
       {:size   (when (not= type :account) :paragraph-2)
        :weight (when (= type :account) :semi-bold)
        :style  {:color (when (not= type :account)
                          (colors/theme-colors colors/neutral-50 colors/neutral-40 theme))}}
       (:address account-props)]]]]
   (when networks?
     [:<>
      [rn/view
       {:style (style/line-divider theme)}]
      [networks values theme]])])

(def view (quo.theme/with-theme view-internal))
