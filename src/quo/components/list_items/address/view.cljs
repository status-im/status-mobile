(ns quo.components.list-items.address.view
  (:require
    [clojure.string :as string]
    [quo.components.avatars.wallet-user-avatar.view :as wallet-user-avatar]
    [quo.components.list-items.address.style :as style]
    [quo.components.markdown.text :as text]
    [quo.foundations.colors :as colors]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]
    [reagent.core :as reagent]))

(defn- left-container
  [{:keys [theme address networks blur?]}]
  [rn/view {:style style/left-container}
   [wallet-user-avatar/wallet-user-avatar
    {:size       :medium
     :f-name     "0"
     :l-name     "x"
     :monospace? true
     :uppercase? false}]
   [rn/view {:style style/account-container}
    [text/text {:size :paragraph-1}
     (map (fn [network]
            ^{:key (str network)}
            [text/text
             {:size   :paragraph-1
              :weight :semi-bold
              :style  {:color (colors/resolve-color network)}} (str (subs (name network) 0 3) ":")])
          networks)
     [text/text
      {:size   :paragraph-1
       :weight :monospace
       :style  {:color (if blur?
                         colors/white
                         (colors/theme-colors colors/neutral-100 colors/white theme))}}
      (string/replace address "x" "×")]]]])

(defn- internal-view
  []
  (let [state       (reagent/atom :default)
        active?     (atom false)
        timer       (atom nil)
        on-press-in (fn []
                      (when-not (= @state :selected)
                        (reset! timer (js/setTimeout #(reset! state :pressed) 100))))]
    (fn [{:keys [networks address customization-color on-press active-state? blur? theme]
          :or   {customization-color :blue}}]
      (let [on-press-out (fn []
                           (let [new-state (if (or (not active-state?) @active?) :default :active)]
                             (when @timer (js/clearTimeout @timer))
                             (reset! timer nil)
                             (reset! active? (= new-state :active))
                             (reset! state new-state)))]
        [rn/pressable
         {:style               (style/container @state customization-color blur?)
          :on-press-in         on-press-in
          :on-press-out        on-press-out
          :on-press            on-press
          :accessibility-label :container}
         [left-container
          {:theme    theme
           :networks networks
           :address  address}]]))))

(def view (quo.theme/with-theme internal-view))
