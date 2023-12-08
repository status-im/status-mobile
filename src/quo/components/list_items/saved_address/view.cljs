(ns quo.components.list-items.saved-address.view
  (:require
    [clojure.string :as string]
    [quo.components.avatars.wallet-user-avatar.view :as wallet-user-avatar]
    [quo.components.icon :as icon]
    [quo.components.list-items.saved-address.style :as style]
    [quo.components.markdown.text :as text]
    [quo.foundations.colors :as colors]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]
    [reagent.core :as reagent]
    [utils.address :as address]))

(defn- left-container
  [{:keys [blur? theme name ens address customization-color]}]
  (let [names      (remove string/blank? (string/split name " "))
        first-name (if (> (count names) 0) (first names) "")
        last-name  (if (> (count names) 1) (last names) "")]
    [rn/view {:style style/left-container}
     [wallet-user-avatar/wallet-user-avatar
      {:size                :medium
       :f-name              first-name
       :l-name              last-name
       :customization-color customization-color}]
     [rn/view {:style style/account-container}
      [text/text
       {:weight :semi-bold
        :size   :paragraph-1
        :style  style/name-text}
       name]
      [text/text {:size :paragraph-2}
       [text/text
        {:size   :paragraph-2
         :weight :monospace
         :style  (style/account-address blur? theme)}
        (or ens (address/get-shortened-key address))]]]]))

(defn- internal-view
  []
  (let [state       (reagent/atom :default)
        active?     (atom false)
        timer       (atom nil)
        on-press-in (fn []
                      (when-not (= @state :selected)
                        (reset! timer (js/setTimeout #(reset! state :pressed) 100))))]
    (fn [{:keys [blur? user-props active-state? customization-color
                 on-press
                 on-options-press
                 theme]
          :or   {customization-color :blue
                 blur?               false}}]
      (let [on-press-out (fn []
                           (let [new-state (if (or (not active-state?) @active?) :default :active)]
                             (when @timer (js/clearTimeout @timer))
                             (reset! timer nil)
                             (reset! active? (= new-state :active))
                             (reset! state new-state)))]
        [rn/pressable
         {:style               (style/container
                                {:state @state :blur? blur? :customization-color customization-color})
          :on-press-in         on-press-in
          :on-press-out        on-press-out
          :on-press            on-press
          :accessibility-label :container}
         [left-container
          {:blur?               blur?
           :theme               theme
           :name                (:name user-props)
           :ens                 (:ens user-props)
           :address             (:address user-props)
           :customization-color (or (:customization-color user-props) :blue)}]
         (when on-options-press
           [rn/pressable
            {:accessibility-label :options-button
             :on-press            on-options-press}
            [icon/icon :i/options
             {:color (if blur?
                       colors/white-opa-70
                       (colors/theme-colors colors/neutral-50 colors/neutral-40 theme))}]])]))))

(def view (quo.theme/with-theme internal-view))
