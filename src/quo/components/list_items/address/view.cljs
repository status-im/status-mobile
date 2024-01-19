(ns quo.components.list-items.address.view
  (:require
    [quo.components.avatars.wallet-user-avatar.view :as wallet-user-avatar]
    [quo.components.list-items.address.style :as style]
    [quo.components.markdown.text :as text]
    [quo.foundations.colors :as colors]
    [quo.theme :as quo.theme]
    [react-native.pure :as rn.pure]
    [utils.address :as address]))

(defn- left-container
  [{:keys [theme address networks blur?]}]
  (rn.pure/view
   {:style style/left-container}
   (wallet-user-avatar/wallet-user-avatar
    {:size       :size-32
     :full-name  "0 x"
     :monospace? true
     :lowercase? true
     :neutral?   true})
   (rn.pure/view
    {:style style/account-container}
    (text/text
     {:size :paragraph-1}
     (map (fn [network]
            ^{:key (str network)}
            (text/text
             {:size   :paragraph-1
              :weight :semi-bold
              :style  {:color (colors/resolve-color (:network-name network) theme)}}
             (str (:short-name network) ":")))
          networks)
     (text/text
      {:size   :paragraph-1
       :weight :monospace
       :style  {:color (if blur?
                         colors/white
                         (colors/theme-colors colors/neutral-100 colors/white theme))}}
      (address/get-shortened-key address))))))

(defn- view-pure
  [{:keys [networks address customization-color on-press active-state? blur?]
    :or   {customization-color :blue}}]
  (let [theme             (quo.theme/use-theme)
        [state set-state] (rn.pure/use-state :default)
        active?           (atom false)
        timer             (atom nil)
        on-press-in       (fn []
                            (when-not (= state :selected)
                              (reset! timer (js/setTimeout #(set-state :pressed) 100))))
        on-press-out      (fn []
                            (let [new-state (if (or (not active-state?) @active?) :default :active)]
                              (when @timer (js/clearTimeout @timer))
                              (reset! timer nil)
                              (reset! active? (= new-state :active))
                              (set-state new-state)))]
    (rn.pure/pressable
     {:style               (style/container state customization-color blur?)
      :on-press-in         on-press-in
      :on-press-out        on-press-out
      :on-press            on-press
      :accessibility-label :container}
     (left-container
      {:theme    theme
       :networks networks
       :address  address
       :blur?    blur?}))))

(defn view [params] (rn.pure/func view-pure params))
