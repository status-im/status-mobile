(ns legacy.status-im.ui.components.bottom-panel.views
  (:require
    ["react-native" :refer (BackHandler)]
    [legacy.status-im.ui.components.animation :as anim]
    [legacy.status-im.ui.components.colors :as colors]
    [legacy.status-im.ui.components.react :as react]
    [react-native.platform :as platform]
    [reagent.core :as reagent])
  (:require-macros [legacy.status-im.utils.views :as views]))

(def back-listener (atom nil))

(defn remove-back-listener
  []
  (when @back-listener
    (.remove ^js @back-listener)
    (reset! back-listener nil)))

(defn add-back-listener
  []
  (remove-back-listener)
  (reset! back-listener (.addEventListener BackHandler
                                           "hardwareBackPress"
                                           (fn [] true))))

(defn hide-panel-anim
  [bottom-anim-value alpha-value window-height]
  (remove-back-listener)
  (react/dismiss-keyboard!)
  (anim/start
   (anim/parallel
    [(anim/spring bottom-anim-value
                  {:toValue         (- window-height)
                   :useNativeDriver true})
     (anim/timing alpha-value
                  {:toValue         0
                   :duration        500
                   :useNativeDriver true})])))

(defn show-panel-anim
  [bottom-anim-value alpha-value]
  (add-back-listener)
  (anim/start
   (anim/parallel
    [(anim/spring bottom-anim-value
                  {:toValue         40
                   :useNativeDriver true})
     (anim/timing alpha-value
                  {:toValue         0.4
                   :duration        500
                   :useNativeDriver true})])))

(defn bottom-panel
  [_ render window-height on-close on-touch-outside show-overlay?]
  (let [bottom-anim-value (anim/create-value window-height)
        alpha-value       (anim/create-value 0)
        clear-timeout     (atom nil)
        update?           (atom nil)
        current-obj       (reagent/atom nil)]
    (reagent/create-class
     {:UNSAFE_componentWillMount  (fn [^js args]
                                    (let [[_ obj _ _] (.-argv (.-props args))]
                                      (when @clear-timeout (js/clearTimeout @clear-timeout))
                                      (when (or (not= obj @current-obj) @update?)
                                        (cond
                                          @update?
                                          (do (reset! update? false)
                                              (show-panel-anim bottom-anim-value alpha-value))

                                          (and @current-obj obj)
                                          (do (reset! update? true)
                                              (js/setTimeout #(reset! current-obj obj) 600)
                                              (hide-panel-anim bottom-anim-value
                                                               alpha-value
                                                               (- window-height)))

                                          obj
                                          (do (reset! current-obj obj)
                                              (show-panel-anim bottom-anim-value alpha-value))

                                          :else
                                          (do (reset! clear-timeout (js/setTimeout #(reset! current-obj
                                                                                      nil)
                                                                                   600))
                                              (hide-panel-anim bottom-anim-value
                                                               alpha-value
                                                               (- window-height)))))))
      :UNSAFE_componentWillUpdate (fn [_ [_ obj _ _]]
                                    (when @clear-timeout (js/clearTimeout @clear-timeout))
                                    (when (or (not= obj @current-obj) @update?)
                                      (cond
                                        @update?
                                        (do (reset! update? false)
                                            (show-panel-anim bottom-anim-value alpha-value))

                                        (and @current-obj obj)
                                        (do (reset! update? true)
                                            (js/setTimeout #(reset! current-obj obj) 600)
                                            (hide-panel-anim bottom-anim-value
                                                             alpha-value
                                                             (- window-height)))

                                        obj
                                        (do (reset! current-obj obj)
                                            (show-panel-anim bottom-anim-value alpha-value))

                                        :else
                                        (do (reset! clear-timeout (js/setTimeout #(reset! current-obj
                                                                                    nil)
                                                                                 600))
                                            (hide-panel-anim bottom-anim-value
                                                             alpha-value
                                                             (- window-height))))))
      :reagent-render             (fn []
                                    (if @current-obj
                                      [react/keyboard-avoiding-view
                                       {:style {:position :absolute :top 0 :bottom 0 :left 0 :right 0}
                                        :ignore-offset true}

                                       [react/view {:flex 1}
                                        (when (and platform/ios? show-overlay?)
                                          [react/animated-view
                                           {:flex             1
                                            :background-color colors/black-persist
                                            :opacity          alpha-value}])
                                        (when on-touch-outside
                                          [react/touchable-opacity
                                           {:active-opacity 0
                                            :on-press       on-touch-outside
                                            :style          {:flex 1}}])
                                        [react/animated-view
                                         {:style {:position  :absolute
                                                  :transform [{:translateY bottom-anim-value}]
                                                  :bottom    0
                                                  :left      0
                                                  :right     0}}
                                         [react/view {:flex 1}
                                          [render @current-obj]]]]]
                                      ;;TODO this is not great, improve!
                                      #(do (on-close)
                                           nil)))})))

(views/defview animated-bottom-panel
  [m view on-close on-touch-outside show-overlay?]
  (views/letsubs [{window-height :height} [:dimensions/window]]
    [bottom-panel
     (when m
       (select-keys m
                    [:from :contact :amount :token :approve? :message :cancel? :hash :name :url :icons
                     :wc-version :params :connector :description :topic :relay :self :peer :permissions
                     :state])) view window-height on-close on-touch-outside
     (if-not (nil? show-overlay?) show-overlay? true)]))
