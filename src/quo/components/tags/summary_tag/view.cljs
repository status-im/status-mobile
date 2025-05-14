(ns quo.components.tags.summary-tag.view
  (:require
    [quo.components.avatars.account-avatar.view :as account-avatar]
    [quo.components.avatars.user-avatar.view :as user-avatar]
    [quo.components.avatars.wallet-user-avatar.view :as wallet-user-avatar]
    [quo.components.markdown.text :as text]
    [quo.components.tags.summary-tag.style :as style]
    [quo.components.utilities.token.view :as token]
    [quo.context :as quo.context]
    [quo.foundations.colors :as colors]
    [react-native.core :as rn]))

(defn- left-view
  [{:keys [label type customization-color emoji image-source token]}]
  (case type
    :account
    [account-avatar/view
     {:customization-color customization-color
      :size                24
      :emoji               emoji
      :type                :default}]
    :network
    [rn/image
     {:source image-source
      :style  style/network}]
    :dapp
    [rn/image
     {:source image-source
      :style  style/dapp}]
    :saved-address
    [wallet-user-avatar/wallet-user-avatar
     {:full-name           label
      :size                :size-24
      :customization-color customization-color}]
    :collectible
    [rn/image
     {:source image-source
      :style  style/collectible-image}]
    :user
    [user-avatar/user-avatar
     {:full-name           label
      :size                :xs
      :profile-picture     image-source
      :customization-color customization-color}]
    :token
    [token/view
     {:token token
      :size  :size-24
      :style style/token-image}]
    nil))

(defn view
  "Options:
    - :label - string - tag label
    - :customization-color - color - It will be passed down to components that
      should vary based on a custom color.
    - :type - :token / :user / :collectible / :saved-address / :network / :account / :dapp
    - :emoji - string - emoji used for displaying account avatar
    - :image-source - resource - image to display on :network, :collectible and :user
    - :theme - :light / :dark"
  [{:keys [label customization-color type container-style]
    :as   props
    :or   {customization-color colors/neutral-80-opa-5}}]
  (let [theme (quo.context/use-theme)]
    [rn/view
     {:accessibility-label :container
      :style               (merge (style/main (assoc props :customization-color customization-color)
                                              theme)
                                  container-style)}
     [left-view props]
     [text/text
      {:style           (style/label type theme)
       :ellipsize-mode  :tail
       :weight          :semi-bold
       :number-of-lines 1
       :size            :heading-1}
      label]]))
