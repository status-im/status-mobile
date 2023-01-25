(ns status-im2.contexts.chat.photo-selector.album-selector.view
  (:require
   [quo2.core :as quo]
   [react-native.core :as rn]
   [utils.i18n :as i18n]
   [utils.re-frame :as rf]
   [quo2.foundations.colors :as colors]
   [status-im2.contexts.chat.photo-selector.view :refer [album-title]]
   [status-im2.contexts.chat.photo-selector.album-selector.style :as style]))

(defn album
  [{:keys [title count uri]} _ _ selected-album]
  (let [selected? (= selected-album title)]
    [rn/touchable-opacity
     {:on-press (fn []
                  (rf/dispatch [:chat.ui/camera-roll-select-album title])
                  (rf/dispatch [:navigate-back]))
      :style    (style/album-container selected?)}
     [rn/image
      {:source {:uri uri}
       :style  style/cover}]
     [rn/view {:style {:margin-left 12}}
      [quo/text {:weight :medium} title]
      [quo/text
       {:size  :paragraph-2
        :style {:color (colors/theme-colors colors/neutral-50 colors/neutral-40)}}
       (str count " " (i18n/label :t/images))]]
     (when selected?
       [rn/view
        {:style {:position :absolute
                 :right    16}}
        [quo/icon :i/check
         {:size 20 :color (colors/theme-colors colors/primary-50 colors/primary-60)}]])]))

(defn section-header
  [{:keys [title]}]
  (when (not= title "smart-albums")
    [quo/divider-label
     {:label           title
      :container-style style/divider}]))

(defn album-selector
  []
  [:f>
   (fn []
     (let [albums         (rf/sub [:camera-roll/albums])
           selected-album (or (rf/sub [:camera-roll/selected-album]) (i18n/label :t/recent))]
       (rn/use-effect-once
        (fn []
          (rf/dispatch [:chat.ui/camera-roll-get-albums])
          js/undefined))
       [rn/view {:style {:padding-top 20}}
        [album-title false selected-album]
        [rn/section-list
         {:data                           albums
          :render-fn                      album
          :render-data                    selected-album
          :sections                       albums
          :sticky-section-headers-enabled false
          :render-section-header-fn       section-header
          :style                          {:margin-top 12}
          :content-container-style        {:padding-bottom 40}
          :key-fn                         :title}]]))])
