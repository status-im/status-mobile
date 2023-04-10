(ns status-im2.contexts.chat.messages.bottom-sheet-composer.actions.view
  (:require
    [quo2.core :as quo]
    [quo2.foundations.colors :as colors]
    [react-native.background-timer :as background-timer]
    [react-native.core :as rn]
    [react-native.permissions :as permissions]
    [react-native.reanimated :as reanimated]
    [reagent.core :as reagent]
    [status-im2.common.alert.events :as alert]
    [status-im2.contexts.chat.messages.bottom-sheet-composer.constants :as c]
    [status-im2.contexts.chat.messages.list.view :as messages.list]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]
    [status-im2.contexts.chat.messages.bottom-sheet-composer.actions.style :as style]))


;;; CONTROLS
(defn send-button
  [input-ref text-value images? window-height
   {:keys [height saved-height last-height opacity background-y container-opacity]}]
  [:f>
   (fn []
     (let [btn-opacity (reanimated/use-shared-value 0)
           z-index     (reagent/atom 0)]
       [:f>
        (fn []
          (rn/use-effect (fn []
                           (if (or (not-empty @text-value) images?)
                             (when-not (= @z-index 1)
                               (reset! z-index 1)
                               (js/setTimeout #(reanimated/animate btn-opacity 1) 50))
                             (when-not (= @z-index 0)
                               (reanimated/animate btn-opacity 0)
                               (js/setTimeout #(reset! z-index 0) 300))))
                         [@text-value])
          [reanimated/view
           {:style (reanimated/apply-animations-to-style
                    {:opacity btn-opacity}
                    {:position         :absolute
                     :right            0
                     :z-index          @z-index
                     :background-color (colors/theme-colors colors/white colors/neutral-95)})}
           [quo/button
            {:icon                true
             :size                32
             :accessibility-label :send-message-button
             :on-press            (fn []
                                    (reanimated/animate height c/input-height)
                                    (reanimated/set-shared-value saved-height c/input-height)
                                    (reanimated/set-shared-value last-height c/input-height)
                                    (reanimated/animate opacity 0)
                                    (js/setTimeout #(reanimated/animate container-opacity 0.7) 300)
                                    (js/setTimeout #(reanimated/set-shared-value background-y
                                                                                 (- window-height))
                                                   300)
                                    (rf/dispatch [:chat.ui/send-current-message])
                                    (rf/dispatch [:chat.ui/set-input-maximized false])
                                    (rf/dispatch [:chat.ui/set-input-content-height c/input-height])
                                    (reset! text-value "")
                                    (.clear ^js @input-ref)
                                    (messages.list/scroll-to-bottom))}
            :i/arrow-up]])]))])

(defn audio-button
  []
  [quo/button
   {:on-press #(js/alert "to be added")
    :icon     true
    :type     :outline
    :size     32}
   :i/audio])
(defn camera-button
  []
  [quo/button
   {:on-press #(js/alert "to be implemented")
    :icon     true
    :type     :outline
    :size     32
    :style    {:margin-right 12}}
   :i/camera])
(defn image-button
  [insets height]
  [quo/button
   {:on-press (fn []
                (permissions/request-permissions
                 {:permissions [:read-external-storage :write-external-storage]
                  :on-allowed  (fn []
                                 (rf/dispatch [:chat.ui/set-input-content-height
                                               (reanimated/get-shared-value height)])
                                 (rf/dispatch [:open-modal :photo-selector {:insets insets}]))
                  :on-denied   (fn []
                                 (background-timer/set-timeout
                                  #(alert/show-popup (i18n/label :t/error)
                                                     (i18n/label
                                                      :t/external-storage-denied))
                                  50))}))
    :icon     true
    :type     :outline
    :size     32
    :style    {:margin-right 12}}
   :i/image])

(defn reaction-button
  []
  [quo/button
   {:on-press #(js/alert "to be implemented")
    :icon     true
    :type     :outline
    :size     32
    :style    {:margin-right 12}}
   :i/reaction])

(defn format-button
  []
  [quo/button
   {:on-press #(js/alert "to be implemented")
    :icon     true
    :type     :outline
    :size     32}
   :i/format])

(defn view
  [input-ref text-value images? {:keys [height] :as animations}
   window-height insets]
  [rn/view {:style (style/actions-container)}
   [rn/view {:style {:flex-direction :row}}
    [camera-button]
    [image-button insets height]
    [reaction-button]
    [format-button]]
   [send-button input-ref text-value images? window-height animations]
   [audio-button]])
