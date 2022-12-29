(ns status-im.ui.screens.onboarding.intro.views
  (:require [quo.animated :as animated]
            [quo.core :as quo]
            [quo.design-system.colors :as colors]
            [quo.design-system.typography :as typography]
            [quo.react-native :as rn]
            [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [i18n.i18n :as i18n]
            [status-im.react-native.resources :as resources]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.onboarding.intro.styles :as styles]))

(defonce index (reagent/atom 0))

(defn code
  [val]
  ^{:key val}
  [animated/code {:exec val}])

(defn dot
  []
  (let [active            (animated/value 0)
        active-transition (animated/with-timing-transition active {:duration 100})]
    (fn [{:keys [selected progress]}]
      [animated/view {:style (styles/dot-style active-transition)}
       [code (animated/set active (if selected 1 0))]
       [animated/view {:style (styles/dot-progress active-transition progress)}]])))

(defn dots-selector
  [{:keys [n progress]}]
  (let [selected @index]
    [react/view {:style (styles/dot-selector)}
     (for [i (range n)]
       ^{:key i}
       [dot
        {:progress progress
         :selected (= selected i)}])]))

(defn slides-view
  [slides width]
  (let [height           (reagent/atom 0)
        text-height      (reagent/atom 0)
        text-temp-height (atom 0)
        text-temp-timer  (atom nil)]
    (fn [_]
      [:<>
       (doall
        (for [s slides]
          ^{:key (:title s)}
          [react/view
           {:style {:flex               1
                    :width              width
                    :justify-content    :flex-end
                    :align-items        :center
                    :padding-horizontal 32}}
           (let [size (min width @height)]
             [react/view
              {:style     {:flex 1}
               :on-layout (fn [^js e]
                            (let [new-height (-> e .-nativeEvent .-layout .-height)]
                              (swap! height #(if (pos? %) (min % new-height) new-height))))}
              [react/image
               {:source      (:image s)
                :resize-mode :contain
                :style       {:width  size
                              :height size}}]])
           [quo/text
            {:style  styles/wizard-title
             :align  :center
             :weight :bold
             :size   :x-large}
            (i18n/label (:title s))]
           [quo/text
            {:style     (styles/wizard-text-with-height @text-height)
             :on-layout
             (fn [^js e]
               (let [new-height (-> e .-nativeEvent .-layout .-height)]
                 (when (and (not= new-height @text-temp-height)
                            (not (zero? new-height))
                            (< new-height 200))
                   (swap! text-temp-height #(if (pos? %) (max % new-height) new-height))
                   (when @text-temp-timer (js/clearTimeout @text-temp-timer))
                   (reset! text-temp-timer
                     (js/setTimeout #(reset! text-height @text-temp-height) 500)))))}
            (i18n/label (:text s))]]))])))

(defn carousel
  [slides width]
  ;;TODO this is really not the best implementation, must be a better way
  (let [scroll-x          (reagent/atom 0)
        scroll-view-ref   (atom nil)

        manual-scroll     (atom false)
        progress          (animated/value 1)
        autoscroll        (animated/value 1)
        finished          (animated/value 0)
        clock             (animated/clock)
        go-next           (fn []
                            (let [x (if (>= @scroll-x
                                            (- (* (dec (count slides))
                                                  width)
                                               5))
                                      0
                                      (+ @scroll-x width))]
                              (reset! index (Math/round (/ x width)))
                              (some-> ^js @scroll-view-ref
                                      (.scrollTo #js {:x x :animated true}))))
        code              (animated/block
                           [(animated/cond* (animated/and* (animated/not* (animated/clock-running clock))
                                                           autoscroll)
                                            (animated/start-clock clock))
                            (animated/cond* (animated/and* (animated/clock-running clock)
                                                           (animated/not* autoscroll))
                                            [(animated/stop-clock clock)
                                             (animated/set finished 1)])
                            (animated/set progress
                                          (animated/cancelable-loop
                                           {:clock    clock
                                            :finished finished
                                            :duration 4000
                                            :on-reach go-next}))])
        cancel-animation  (fn []
                            (reset! manual-scroll true)
                            (animated/set-value autoscroll 0))
        restart-animation (fn []
                            (animated/set-value autoscroll 1))]
    (fn [_ _]
      [react/view
       {:style {:align-items     :center
                :flex            1
                :justify-content :flex-end}}
       [animated/code {:exec code}]
       [react/scroll-view
        {:horizontal                        true
         :paging-enabled                    true
         :ref                               #(reset! scroll-view-ref %)
         :shows-vertical-scroll-indicator   false
         :shows-horizontal-scroll-indicator false
         :pinch-gesture-enabled             false
         :scroll-event-throttle             16
         :on-scroll                         #(let [x (.-nativeEvent.contentOffset.x ^js %)]
                                               (when @manual-scroll
                                                 ;; NOTE: Will be not synced if velocity is big
                                                 (reset! index (Math/round (/ x width))))
                                               (reset! scroll-x x))
         :on-scroll-begin-drag              cancel-animation
         :on-scroll-end-drag                restart-animation
         :on-momentum-scroll-end            #(reset! manual-scroll false)
         :style                             {:margin-bottom 16}}
        [slides-view slides width]]
       [dots-selector
        {:progress progress
         :n        (count slides)}]])))

(defonce tos-accepted (reagent/atom false))

(defn intro
  []
  [react/view {:style styles/intro-view}
   [carousel
    [{:image (resources/get-theme-image :chat)
      :title :intro-title1
      :text  :intro-text1}
     {:image (resources/get-theme-image :wallet)
      :title :intro-title2
      :text  :intro-text2}
     {:image (resources/get-theme-image :browser)
      :title :intro-title3
      :text  :intro-text3}]
    @(re-frame/subscribe [:dimensions/window-width])]
   [react/view {:style {:align-items :center}}
    [react/view
     {:flex-direction  :row
      :justify-content :space-between
      :align-items     :center
      :margin-top      36
      :margin-bottom   24}
     [quo/checkbox
      {:value     @tos-accepted
       :on-change #(swap! tos-accepted not)}]
     [rn/touchable-opacity {:test-ID :terms-of-service :on-press #(swap! tos-accepted not)}
      [react/nested-text {:style {:margin-left 12}}
       (i18n/label :t/accept-status-tos-prefix)
       [{:style               (merge {:color colors/blue}
                                     typography/font-medium)
         :on-press            #(re-frame/dispatch [:open-modal :terms-of-service])
         :accessibility-label :terms-of-service-link}
        " "
        (i18n/label :t/terms-of-service)]]]]
    [react/view {:style {:margin-bottom 24}}
     [quo/button
      {:test-ID  :get-started
       :disabled (not @tos-accepted)
       :on-press #(do (re-frame/dispatch [:init-root :onboarding])
                      ;; clear atom state for next use
                      (reset! tos-accepted false)
                      (re-frame/dispatch [:hide-terms-of-services-opt-in-screen]))}
      (i18n/label :t/get-started)]]
    [react/text
     {:style               {:color colors/blue}
      :on-press            #(re-frame/dispatch [:open-modal :privacy-policy])
      :accessibility-label :privacy-policy-link}
     (i18n/label :t/privacy-policy)]]])
