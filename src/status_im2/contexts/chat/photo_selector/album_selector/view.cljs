(ns status-im2.contexts.chat.photo-selector.album-selector.view
  (:require
    [quo2.core :as quo]
    [react-native.core :as rn]
    [react-native.gesture :as gesture]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]
    [quo2.foundations.colors :as colors]
    [status-im2.contexts.chat.photo-selector.album-selector.style :as style]))

(defn render-album
  [{:keys [title count uri]} index _ {:keys [album? selected-album]}]
  (let [selected? (= selected-album title)]
    [rn/touchable-opacity
     {:on-press            (fn []
                             (rf/dispatch [:chat.ui/camera-roll-select-album title])
                             (rf/dispatch [:photo-selector/get-photos-for-selected-album 20])
                             (reset! album? false))
      :style               (style/album-container selected?)
      :accessibility-label (str "album-" index)}
     [rn/image
      {:source {:uri uri}
       :style  style/cover}]
     [rn/view {:style {:margin-left 12}}
      [quo/text
       {:weight          :medium
        :ellipsize-mode  :tail
        :number-of-lines 1
        :style           {:margin-right 50}}
       title]

      [quo/text
       {:size  :paragraph-2
        :style {:color (colors/theme-colors colors/neutral-50 colors/neutral-40)}}
       (when count (str count " " (i18n/label :t/images)))]]
     (when selected?
       [rn/view
        {:style {:position :absolute
                 :right    16}}
        [quo/icon :i/check
         {:size 20 :color (colors/theme-colors colors/primary-50 colors/primary-60)}]])]))

(def no-title "no-title")

(defn section-header
  [{:keys [title]}]
  (when-not (= title no-title)
    [quo/divider-label
     {:label           title
      :container-style style/divider}]))

(defn key-fn
  [item index]
  (str (:title item) index))

(defn album-selector
  [{:keys [scroll-enabled on-scroll]} album? selected-album]
  (let [albums          (rf/sub [:camera-roll/albums])
        albums-sections [{:title no-title :data (:smart-albums albums)}
                         {:title (i18n/label :t/my-albums) :data (:my-albums albums)}]]
    [gesture/section-list
     {:data                           albums-sections
      :sections                       albums-sections
      :render-data                    {:album? album? :selected-album selected-album}
      :render-fn                      render-album
      :sticky-section-headers-enabled false
      :render-section-header-fn       section-header
      :content-container-style        {:padding-top    64
                                       :padding-bottom 40}
      :key-fn                         key-fn
      :scroll-enabled                 @scroll-enabled
      :on-scroll                      on-scroll}]))
