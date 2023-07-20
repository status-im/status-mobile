(ns quo2.components.notifications.toast.view
  (:require [quo2.components.avatars.user-avatar.view :as user-avatar]
            [quo2.components.icon :as icon]
            [quo2.components.markdown.text :as text]
            [quo2.components.notifications.count-down-circle :as count-down-circle]
            [quo2.components.notifications.toast.style :as style]
            [quo2.theme :as quo.theme]
            [quo2.foundations.blur.view :as quo.blur]
            [react-native.core :as rn]
            [utils.i18n :as i18n]))

(defn toast-action-container
  [{:keys [on-press style]} & children]
  [rn/touchable-highlight
   {:on-press       on-press
    :underlay-color :transparent}
   [into
    [rn/view
     {:style (merge style/action-container style)}]
    children]])

(defn toast-undo-action
  [duration on-press theme]
  [toast-action-container {:on-press on-press :accessibility-label :toast-undo-action}
   [rn/view {:style {:margin-right 5}}
    [count-down-circle/circle-timer {:duration duration}]]
   [text/text
    {:size :paragraph-2 :weight :medium :style (style/text theme)}
    [i18n/label :t/undo]]])

(defn- toast-container
  [{:keys [left title text right container-style theme]}]
  [rn/view {:style (merge style/box-container container-style)}

   [quo.blur/view
    {:blur-type       (quo.theme/theme-value :blur-dark :blur-light theme)
     :container-style style/blur-container}]
   [rn/view {:style style/content-container}
    [rn/view {:style style/left-side-container}
     left]
    [rn/view {:style style/right-side-container}
     (when title
       [text/text
        {:size                :paragraph-1
         :weight              :semi-bold
         :style               (style/title theme)
         :accessibility-label :toast-title}
        title])
     (when text
       [text/text
        {:size                :paragraph-2
         :weight              :medium
         :style               (style/text theme)
         :accessibility-label :toast-content}
        text])]
    right]])

(defn- toast-internal
  [{:keys [icon icon-color title text action undo-duration undo-on-press container-style
           theme user]}]
  [toast-container
   {:left            (cond icon
                           [icon/icon icon
                            (cond-> (style/icon theme)
                              icon-color
                              (assoc :color icon-color))]

                           user
                           [user-avatar/user-avatar user])
    :title           title
    :text            text
    :right           (if undo-duration
                       [toast-undo-action undo-duration undo-on-press theme]
                       action)
    :container-style container-style
    :theme           theme}])

(def toast (quo.theme/with-theme toast-internal))
