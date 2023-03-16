(ns status-im2.contexts.chat.bottom-sheet-composer.actions.view
  (:require
    [quo2.core :as quo]
    [react-native.core :as rn]
    [react-native.permissions :as permissions]
    [react-native.platform :as platform]
    [react-native.reanimated :as reanimated]
    [reagent.core :as reagent]
    [status-im2.common.alert.events :as alert]
    [status-im2.contexts.chat.bottom-sheet-composer.constants :as c]
    [status-im2.contexts.chat.messages.list.view :as messages.list]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]
    [status-im2.contexts.chat.bottom-sheet-composer.actions.style :as style]))

(defn send-message
  [{:keys [input-ref]}
   {:keys [text-value focused?]}
   {:keys [height saved-height last-height opacity background-y container-opacity]}
   window-height]
  (reanimated/animate height c/input-height)
  (reanimated/set-shared-value saved-height c/input-height)
  (reanimated/set-shared-value last-height c/input-height)
  (reanimated/animate opacity 0)
  (when-not @focused?
    (js/setTimeout #(reanimated/animate container-opacity 0.7) 300))
  (js/setTimeout #(reanimated/set-shared-value background-y
                                               (- window-height))
                 300)
  (rf/dispatch [:chat.ui/send-current-message])
  (rf/dispatch [:chat.ui/set-input-maximized false])
  (rf/dispatch [:chat.ui/set-input-content-height c/input-height])
  (reset! text-value "")
  (.clear ^js @input-ref)
  (messages.list/scroll-to-bottom))

(defn send-button
  [props
   {:keys [text-value] :as state}
   animations
   window-height
   images?]
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
           {:style (style/send-button btn-opacity @z-index)}
           [quo/button
            {:icon                true
             :size                32
             :accessibility-label :send-message-button
             :on-press            #(send-message props state animations window-height)}
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
  [{:keys [input-ref]}
   {:keys [focused?]}
   {:keys [height]}
   insets]
  [quo/button
   {:on-press (fn []
                (permissions/request-permissions
                 {:permissions [:read-external-storage :write-external-storage]
                  :on-allowed  (fn []
                                 (when platform/android?
                                   (when @focused?
                                     (rf/dispatch [:chat.ui/set-input-refocus true]))
                                   (.blur ^js @input-ref))
                                 (rf/dispatch [:chat.ui/set-input-content-height
                                               (reanimated/get-shared-value height)])
                                 (rf/dispatch [:open-modal :photo-selector {:insets insets}]))
                  :on-denied   (fn []
                                 (alert/show-popup (i18n/label :t/error)
                                                   (i18n/label
                                                    :t/external-storage-denied)))}))
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
  [props state animations window-height insets images?]
  [rn/view {:style (style/actions-container)}
   [rn/view {:style {:flex-direction :row}}
    [camera-button]
    [image-button props state animations insets]
    [reaction-button]
    [format-button]]
   [send-button props state animations window-height images?]
   [audio-button]])
