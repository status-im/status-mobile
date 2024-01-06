(ns quo.components.wallet.summary-info.view
  (:require
    [quo.components.avatars.account-avatar.view :as account-avatar]
    [quo.components.avatars.user-avatar.view :as user-avatar]
    [quo.components.avatars.wallet-user-avatar.view :as wallet-user-avatar]
    [quo.components.markdown.text :as text]
    [quo.components.wallet.summary-info.style :as style]
    [quo.foundations.colors :as colors]
    [quo.foundations.resources :as resources]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]))

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
  (let [{:keys [ethereum optimism arbitrum]} values
        show-optimism?                       (pos? optimism)
        show-arbitrum?                       (pos? arbitrum)]
    [rn/view
     {:style               style/networks-container
      :accessibility-label :networks}
     (when (pos? ethereum)
       [network-amount
        {:network  :ethereum
         :amount   (str ethereum " ETH")
         :divider? (or show-arbitrum? show-optimism?)
         :theme    theme}])
     (when show-optimism?
       [network-amount
        {:network  :optimism
         :amount   (str optimism " OPT")
         :divider? show-arbitrum?
         :theme    theme}])
     (when show-arbitrum?
       [network-amount
        {:network :arbitrum
         :amount  (str arbitrum " ARB")
         :theme   theme}])]))

(defn- view-internal
  [{:keys [theme type account-props networks? values]}]
  [rn/view
   {:style (style/container networks? theme)}
   [rn/view
    {:style style/info-container}
    (case type
      :status-account [account-avatar/view account-props]
      :saved-account  [wallet-user-avatar/wallet-user-avatar (assoc account-props :size :size-32)]
      :account        [wallet-user-avatar/wallet-user-avatar
                       (assoc account-props
                              :size     :size-32
                              :neutral? true)]
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
