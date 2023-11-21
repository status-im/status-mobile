(ns status-im2.common.top-bar.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [react-native.platform :as platform]
    [react-native.reanimated :as reanimated]
    [status-im2.common.top-bar.style :as style]))

(def header-extrapolation-option
  {:extrapolateLeft  "clamp"
   :extrapolateRight "clamp"})

(defn- add-inverted-y-android
  [style]
  (cond-> style
          platform/android?
          (assoc :scale-y -1)))

;; TODO(alwx): status-im2.contexts.chat.messages.list.view/list-footer should be abstracted
(defn f-abstract-list-header
  [{:keys [inverted-y-on-android?
           show?
           theme
           cover-bg-color
           scroll-y
           on-layout
           avatar-component
           title-component
           description
           actions-component
           loading-component]}]
  (let [border-animation (reanimated/interpolate scroll-y
                                                 [30 125]
                                                 [14 0]
                                                 header-extrapolation-option)]
    [rn/view (cond-> {:flex 1}
                     inverted-y-on-android?
                     (add-inverted-y-android))
     [rn/view {:style     (style/header-container show? theme)
               :on-layout on-layout}
      [rn/view {:style (style/header-cover cover-bg-color theme)}]
      [reanimated/view {:style (style/header-bottom-part border-animation theme)}
       [rn/view {:style style/header-avatar}
        [rn/view {:style {:align-items :flex-start}}
         [avatar-component]]
        [title-component]
        (when description
          [quo/text {:style style/header-description-text}
           description])
        (when actions-component
          [actions-component])]]]
     (when loading-component
       [loading-component])]))

(defn abstract-list-header
  [props]
  [:f> f-abstract-list-header props])