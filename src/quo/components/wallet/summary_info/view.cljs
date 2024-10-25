(ns quo.components.wallet.summary-info.view
  (:require
    [quo.components.avatars.account-avatar.view :as account-avatar]
    [quo.components.avatars.user-avatar.view :as user-avatar]
    [quo.components.avatars.wallet-user-avatar.view :as wallet-user-avatar]
    [quo.components.markdown.text :as text]
    [quo.components.utilities.token.view :as token]
    [quo.components.wallet.summary-info.schema :as summary-info-schema]
    [quo.components.wallet.summary-info.style :as style]
    [quo.foundations.colors :as colors]
    [quo.foundations.resources :as resources]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]
    [schema.core :as schema]))

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
        show-optimism?                       (and optimism
                                                  (or (pos? (:amount optimism))
                                                      (= (:amount optimism) "<0.01")))
        show-arbitrum?                       (and arbitrum
                                                  (or (pos? (:amount arbitrum))
                                                      (= (:amount arbitrum) "<0.01")))]
    [rn/view
     {:style               style/networks-container
      :accessibility-label :networks}
     (when (and ethereum (pos? (:amount ethereum)))
       [network-amount
        {:network  :ethereum
         :amount   (str (:amount ethereum) " " (or (:token-symbol ethereum) "ETH"))
         :divider? (or show-arbitrum? show-optimism?)
         :theme    theme}])
     (when show-optimism?
       [network-amount
        {:network  :optimism
         :amount   (str (:amount optimism) " " (or (:token-symbol optimism) "OETH"))
         :divider? show-arbitrum?
         :theme    theme}])
     (when show-arbitrum?
       [network-amount
        {:network :arbitrum
         :amount  (str (:amount arbitrum) " " (or (:token-symbol arbitrum) "ARB"))
         :theme   theme}])]))

(defn- view-internal
  [{:keys [type account-props token-props networks? values]}]
  (let [theme   (quo.theme/use-theme)
        address (or (:address account-props) (:address token-props))]
    [rn/view
     {:style (style/container networks? theme)}
     [rn/view
      {:style style/info-container}
      (case type
        :token          [token/view (select-keys token-props #{:token :size})]
        :status-account [account-avatar/view account-props]
        :saved-account  [wallet-user-avatar/wallet-user-avatar (assoc account-props :size :size-32)]
        :account        [wallet-user-avatar/wallet-user-avatar
                         (assoc account-props
                                :size     :size-32
                                :neutral? true)]
        [user-avatar/user-avatar account-props])
      [rn/view {:style {:margin-left 8}}
       (when (not= type :account)
         [text/text {:weight :semi-bold} (or (:name account-props) (:label token-props))])
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
        (when address
          [text/text
           {:size   (when (not= type :account) :paragraph-2)
            :weight (when (= type :account) :semi-bold)
            :style  {:color (when (not= type :account)
                              (colors/theme-colors colors/neutral-50 colors/neutral-40 theme))}}
           address])]]]
     (when networks?
       [:<>
        [rn/view
         {:style (style/line-divider theme)}]
        [networks values theme]])]))

(def view (schema/instrument #'view-internal summary-info-schema/?schema))
