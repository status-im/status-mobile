(ns quo2.components.notifications.toast.view
  (:require [quo2.components.avatars.user-avatar.view :as user-avatar]
            [quo2.components.icon :as icon]
            [quo2.components.markdown.text :as text]
            [quo2.components.notifications.count-down-circle :as count-down-circle]
            [quo2.components.notifications.toast.style :as style]
            [quo2.theme :as theme]
            [react-native.blur :as blur]
            [react-native.core :as rn]
            [utils.i18n :as i18n]))

(defn toast-action-container
  [{:keys [on-press style]} & children]
  [rn/touchable-highlight
   {:on-press       on-press
    :underlay-color :transparent}
   [into
    [rn/view
     {:style (merge (style/action-container (theme/get-theme)) style)}]
    children]])

(defn toast-undo-action
  [duration on-press override-theme]
  [toast-action-container
   {:on-press on-press :accessibility-label :toast-undo-action}
   [rn/view {:style {:margin-right 5}}
    [count-down-circle/circle-timer {:duration duration}]]
   [text/text
    {:size :paragraph-2 :weight :medium :style (style/text override-theme)}
    [i18n/label :t/undo]]])

(defn- toast-container
  [{:keys [left title text right container-style override-theme]}]
  [rn/view
   {:style (merge style/box-container container-style)}
   [blur/webview-blur
    {:style style/blur-container
     :blur-radius 10}]
   ;[blur/view
   ; {:style         style/blur-container
   ;  :blur-amount   13
   ;  :blur-radius   10
   ;  :blur-type     :transparent
   ;  :overlay-color :transparent}]
   [rn/view
    {:style (style/content-container override-theme)}
    [rn/view {:style style/left-side-container} left]
    [rn/view {:style style/right-side-container}
     (when title
       [text/text
        {:size                :paragraph-1
         :weight              :semi-bold
         :style               (style/title override-theme)
         :accessibility-label :toast-title}
        title])
     (when text
       [text/text
        {:size                :paragraph-2
         :weight              :medium
         :style               (style/text override-theme)
         :accessibility-label :toast-content}
        text])]
    (when right right)]])

(defn toast
  [{:keys [icon icon-color title text action undo-duration undo-on-press container-style
           override-theme user]}]
  [toast-container
   {:left            (cond icon
                           [icon/icon icon
                            (cond-> (style/icon override-theme)
                              icon-color
                              (assoc :color icon-color))]

                           user
                           [user-avatar/user-avatar user])
    :title           title
    :text            text
    :right           (if undo-duration
                       [toast-undo-action undo-duration undo-on-press override-theme]
                       action)
    :container-style container-style
    :override-theme  override-theme}])
