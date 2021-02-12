(ns status-im.ui.screens.intro.carousel
  (:require [quo.animated :as animated]
            [quo.react-native :as rn]
            [quo.core :as quo]
            [reagent.core :as r]
            [status-im.i18n.i18n :as i18n]
            [status-im.ui.screens.intro.styles :as styles]))

(defn code [val]
  ^{:key val}
  [animated/code {:exec val}])

(defn dot []
  (let [active            (animated/value 0)
        active-transition (animated/with-timing-transition active {:duration 100})]
    (fn [{:keys [selected progress]}]
      [animated/view {:style (styles/dot-style active-transition)}
       [code (animated/set active (if selected 1 0))]
       [animated/view {:style (styles/dot-progress active-transition progress)}]])))

(defn dots-selector [{:keys [n selected progress]}]
  [rn/view {:style (styles/dot-selector)}
   (for [i (range n)]
     ^{:key i}
     [dot {:progress progress
           :selected (= selected i)}])])

(defn viewer [slides window-height _]
  (let [scroll-x          (r/atom 0)
        scroll-view-ref   (atom nil)
        width             (r/atom 0)
        height            (r/atom 0)
        text-height       (r/atom 0)
        index             (r/atom 0)
        manual-scroll     (atom false)
        text-temp-height  (atom 0)
        text-temp-timer   (atom nil)
        bottom-margin     (if (> window-height 600) 32 16)
        progress          (animated/value 1)
        autoscroll        (animated/value 1)
        finished          (animated/value 0)
        clock             (animated/clock)
        go-next           (fn []
                            (let [x (if (>= @scroll-x (* (dec (count slides))
                                                         @width))
                                      0
                                      (+ @scroll-x @width))]
                              (reset! index (Math/round (/ x @width)))
                              (some-> ^js @scroll-view-ref (.scrollTo #js {:x x :animated true}))))
        code              (animated/block
                           [(animated/cond* (animated/and* (animated/not* (animated/clock-running clock))
                                                           autoscroll)
                                            (animated/start-clock clock))
                            (animated/cond* (animated/and* (animated/clock-running clock)
                                                           (animated/not* autoscroll))
                                            [(animated/stop-clock clock)
                                             (animated/set finished 1)])
                            (animated/set progress (animated/cancelable-loop
                                                    {:clock    clock
                                                     :finished finished
                                                     :duration 4000
                                                     :on-reach go-next}))])
        cancel-animation  (fn []
                            (reset! manual-scroll true)
                            (animated/set-value autoscroll 0))
        restart-animation (fn []
                            (animated/set-value autoscroll 1))]
    (fn [_ _ view-id]
      (let [current-screen? (or (nil? view-id) (= view-id :intro))]
        [rn/view {:style     {:align-items     :center
                              :flex            1
                              :margin-bottom   bottom-margin
                              :justify-content :flex-end}
                  :on-layout (fn [^js e]
                               (when current-screen?
                                 (reset! width (-> e .-nativeEvent .-layout .-width))))}
         (when current-screen?
           [animated/code {:exec code}])
         [rn/scroll-view {:horizontal                        true
                          :paging-enabled                    true
                          :ref                               #(reset! scroll-view-ref %)
                          :shows-vertical-scroll-indicator   false
                          :shows-horizontal-scroll-indicator false
                          :pinch-gesture-enabled             false
                          :scroll-event-throttle             16
                          :on-scroll                         #(let [x (.-nativeEvent.contentOffset.x ^js %)]
                                                                (when @manual-scroll
                                                                  ;; NOTE: Will be not synced if velocity is big
                                                                  (reset! index (Math/round (/ x @width))))
                                                                (reset! scroll-x x))
                          :on-scroll-begin-drag              cancel-animation
                          :on-scroll-end-drag                restart-animation
                          :on-momentum-scroll-end            #(reset! manual-scroll false)
                          :style                             {:margin-bottom bottom-margin}}
          (doall
           (for [s slides]
             ^{:key (:title s)}
             [rn/view {:style {:flex               1
                               :width              @width
                               :justify-content    :flex-end
                               :align-items        :center
                               :padding-horizontal 32}}
              (let [size (min @width @height)]
                [rn/view {:style     {:flex 1}
                          :on-layout (fn [^js e]
                                       (let [new-height (-> e .-nativeEvent .-layout .-height)]
                                         (when current-screen?
                                           (swap! height #(if (pos? %) (min % new-height) new-height)))))}
                 [rn/image {:source      (:image s)
                            :resize-mode :contain
                            :style       {:width  size
                                          :height size}}]])
              [quo/text {:style  styles/wizard-title
                         :align  :center
                         :weight :bold
                         :size   :x-large}
               (i18n/label (:title s))]
              [quo/text {:style (styles/wizard-text-with-height @text-height)
                         :on-layout
                         (fn [^js e]
                           (let [new-height (-> e .-nativeEvent .-layout .-height)]
                             (when (and current-screen?
                                        (not= new-height @text-temp-height)
                                        (not (zero? new-height))
                                        (< new-height 200))
                               (swap! text-temp-height #(if (pos? %) (max % new-height) new-height))
                               (when @text-temp-timer (js/clearTimeout @text-temp-timer))
                               (reset! text-temp-timer
                                       (js/setTimeout #(reset! text-height @text-temp-height) 500)))))}
               (i18n/label (:text s))]]))]
         [dots-selector {:selected @index
                         :progress progress
                         :n        (count slides)}]]))))
