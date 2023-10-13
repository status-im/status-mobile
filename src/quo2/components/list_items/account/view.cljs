(ns quo2.components.list-items.account.view
  (:require [quo2.components.avatars.account-avatar.view :as account-avatar]
            [quo2.components.markdown.text :as text]
            [quo2.foundations.colors :as colors]
            [quo2.theme :as quo.theme]
            [react-native.core :as rn]
            [quo2.components.list-items.account.style :as style]
            [reagent.core :as reagent]
            [quo2.components.icon :as icon]))

(defn- network-view
  [network]
  [text/text
   {:size   :paragraph-2
    :weight :regular
    :style  {:color (colors/custom-color (:name network))}}
   (str (name (:short network)) ":")])

(defn- account-view
  [{:keys [account-props title-icon? blur? theme]
    :or   {title-icon? false}}]
  [rn/view {:style style/left-container}
   [account-avatar/view (assoc account-props :size 32)]
   [rn/view {:style style/account-container}
    [rn/view
     {:style style/account-title-container}
     [text/text
      {:weight :semi-bold
       :size   :paragraph-1}
      (:name account-props)]
     (when title-icon?
       [rn/view
        {:style               style/keycard-icon-container
         :accessibility-label :keycard-icon}
        [icon/icon :i/keycard
         {:size  20
          :color (if blur?
                   colors/white-opa-40
                   (colors/theme-colors colors/neutral-50 colors/neutral-40 theme))}]])]
    [text/text {:size :paragraph-2}
     (for [network (:networks account-props)]
       ^{:key (str network)}
       [network-view network])
     [text/text
      {:size   :paragraph-2
       :weight :monospace
       :style  (style/account-address blur? theme)}
      (:address account-props)]]]])

(defn- balance-view
  [{:keys [balance-props type theme]}]
  [rn/view
   {:style               style/balance-container
    :accessibility-label :balance-container}
   [text/text
    {:weight :medium
     :size   :paragraph-2}
    (:fiat-value balance-props)]
   [rn/view
    {:style style/metrics-container}
    [text/text
     {:size  :paragraph-2
      :style (style/metric-text type theme)}
     (str (:percentage-change balance-props) "%")]
    [rn/view {:style (style/dot-divider type theme)}]
    [text/text
     {:size  :paragraph-2
      :style (style/metric-text type theme)}
     (:fiat-change balance-props)]
    (when (not= type :balance-neutral)
      [rn/view
       {:style               style/arrow-icon-container
        :accessibility-label :arrow-icon}
       [icon/icon (if (= type :balance-positive) :i/positive :i/negative)
        (assoc (style/arrow-icon type theme)
               :accessibility-label
               (if (= type :balance-positive) :icon-positive :icon-negative))]])]])

(defn- token-tag
  [{:keys [token-props blur? theme]}]
  ;; TODO: Use Tiny tag component when available (issue #17341)
  [rn/view
   {:style               (style/token-tag-text-container blur? theme)
    :accessibility-label :tag-container}
   [text/text
    {:size   :label
     :weight :medium
     :style  (style/token-tag-text blur? theme)}
    (str (:value token-props) " " (:symbol token-props))]])

(defn- options-button
  [{:keys [on-options-press blur? theme]}]
  [rn/pressable
   {:accessibility-label :options-button
    :on-press            #(when on-options-press
                            (on-options-press))}
   [icon/icon :i/options
    {:color (if blur?
              colors/white-opa-70
              (colors/theme-colors colors/neutral-50 colors/neutral-40 theme))}]])

(defn- check-icon
  [{:keys [blur? customization-color theme]}]
  [rn/view {:accessibility-label :check-icon}
   [icon/icon :i/check
    {:color (if blur?
              colors/white
              (colors/theme-colors (colors/custom-color customization-color 50)
                                   (colors/custom-color customization-color 60)
                                   theme))}]])

(defn- f-internal-view
  []
  (let [state               (reagent/atom :default)
        active-or-selected? (atom false)
        timer               (atom nil)
        on-press-in         (fn []
                              (when-not (= @state :selected)
                                (reset! timer (js/setTimeout #(reset! state :pressed) 100))))]
    (fn [{:keys [type selectable? blur? customization-color on-press]
          :or   {customization-color :blue
                 type                :default
                 blur?               false}
          :as   props}]
      (let [on-press-out (fn []
                           (let [new-state (if @active-or-selected?
                                             :default
                                             (if (and (= type :default) selectable?)
                                               :selected
                                               :active))]
                             (when @timer (js/clearTimeout @timer))
                             (reset! timer nil)
                             (reset! active-or-selected? (or (= new-state :active)
                                                             (= new-state :selected)))
                             (reset! state new-state)
                             (when on-press
                               (on-press))))]
        (rn/use-effect
         #(cond (and selectable? (= type :default) (= @state :active))         (reset! state :selected)
                (and (not selectable?) (= type :default) (= @state :selected)) (reset! state :active))
         [selectable?])
        [rn/pressable
         {:style               (style/container
                                {:state @state :blur? blur? :customization-color customization-color})
          :on-press-in         on-press-in
          :on-press-out        on-press-out
          :accessibility-label :container}
         [account-view props]
         [rn/view {:style (when (= type :tag) style/token-tag-container)}
          (when (or (= type :balance-neutral)
                    (= type :balance-negative)
                    (= type :balance-positive))
            [balance-view props])
          (when (= type :tag)
            [token-tag props])
          (when (= type :action)
            [options-button props])
          (when (and (= type :default)
                     (= @state :selected))
            [check-icon props])]]))))

(defn- internal-view
  [props]
  [:f> f-internal-view props])

(def view (quo.theme/with-theme internal-view))
