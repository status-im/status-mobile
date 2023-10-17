(ns quo2.components.list-items.saved-address.view
  (:require
    [clojure.string :as string]
    [quo2.components.avatars.wallet-user-avatar :as wallet-user-avatar]
    [quo2.components.icon :as icon]
    [quo2.components.list-items.saved-address.style :as style]
    [quo2.components.markdown.text :as text]
    [quo2.foundations.colors :as colors]
    [quo2.theme :as quo.theme]
    [react-native.core :as rn]
    [reagent.core :as reagent]))

(defn- left-container
  [{:keys [blur? theme name address customization-color]}]
  (let [names      (remove string/blank? (string/split name " "))
        first-name (if (> (count names) 0) (first names) "")
        last-name  (if (> (count names) 1) (last names) "")]
    [rn/view {:style style/left-container}
     [wallet-user-avatar/wallet-user-avatar
      {:size   :medium
       :f-name first-name
       :l-name last-name
       :color  customization-color}]
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
        address]]]]))

(defn- internal-view
  []
  (let [state       (reagent/atom :default)
        active?     (atom false)
        timer       (atom nil)
        on-press-in (fn []
                      (when-not (= @state :selected)
                        (reset! timer (js/setTimeout #(reset! state :pressed) 100))))]
    (fn [{:keys [blur? user-props customization-color
                 on-press
                 on-options-press
                 theme]
          :or   {customization-color :blue
                 blur?               false}}]
      (let [on-press-out (fn []
                           (let [new-state (if @active? :default :active)]
                             (when @timer (js/clearTimeout @timer))
                             (reset! timer nil)
                             (reset! active? (= new-state :active))
                             (reset! state new-state)
                             (when on-press
                               (on-press))))]
        [rn/pressable
         {:style               (style/container
                                {:state @state :blur? blur? :customization-color customization-color})
          :on-press-in         on-press-in
          :on-press-out        on-press-out
          :accessibility-label :container}
         [left-container
          {:blur?               blur?
           :theme               theme
           :name                (:name user-props)
           :address             (:address user-props)
           :customization-color (:customization-color user-props)}]
         [rn/pressable
          {:accessibility-label :options-button
           :on-press            #(when on-options-press
                                   (on-options-press))}
          [icon/icon :i/options
           {:color (if blur?
                     colors/white-opa-70
                     (colors/theme-colors colors/neutral-50 colors/neutral-40 theme))}]]]))))

(def view (quo.theme/with-theme internal-view))
