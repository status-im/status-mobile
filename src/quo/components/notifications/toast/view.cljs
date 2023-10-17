(ns quo.components.notifications.toast.view
  (:require
    [quo.components.avatars.user-avatar.view :as user-avatar]
    [quo.components.icon :as icon]
    [quo.components.markdown.text :as text]
    [quo.components.notifications.count-down-circle :as count-down-circle]
    [quo.components.notifications.toast.style :as style]
    [quo.theme :as quo.theme]
    [react-native.blur :as blur]
    [react-native.core :as rn]
    [utils.i18n :as i18n]))

(defn toast-action-container
  [{:keys [on-press style theme]} & children]
  [rn/touchable-highlight
   {:on-press       on-press
    :underlay-color :transparent}
   [into
    [rn/view
     {:style (merge (style/action-container theme) style)}]
    children]])

(defn toast-undo-action-internal
  [{:keys [undo-duration undo-on-press theme]}]
  [toast-action-container
   {:on-press            undo-on-press
    :accessibility-label :toast-undo-action
    :theme               theme}
   [rn/view {:style {:margin-right 5}}
    [count-down-circle/circle-timer {:duration undo-duration}]]
   [text/text
    {:size   :paragraph-2
     :weight :medium
     :style  (style/text theme)}
    [i18n/label :t/undo]]])

(def ^:private toast-undo-action (quo.theme/with-theme toast-undo-action-internal))

(defn- toast-container-internal
  [{:keys [left title text right container-style theme]}]
  [rn/view {:style (merge (style/box-container theme) container-style)}
   [blur/view
    {:style         style/blur-container
     :blur-amount   13
     :blur-radius   10
     :blur-type     :transparent
     :overlay-color :transparent}]

   [rn/view {:style (style/content-container theme)}
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

(def ^:private toast-container (quo.theme/with-theme toast-container-internal))

(defn toast
  [{:keys [icon icon-color title text action undo-duration undo-on-press container-style theme user]}]
  (let [context-theme (or theme (quo.theme/get-theme))]
    [quo.theme/provider {:theme context-theme}
     [toast-container
      {:left            (cond icon
                              [icon/icon icon
                               (cond-> (style/icon context-theme)
                                 icon-color
                                 (assoc :color icon-color))]
                              user
                              [user-avatar/user-avatar user])
       :title           title
       :text            text
       :right           (if undo-duration
                          [toast-undo-action
                           {:undo-duration undo-duration
                            :undo-on-press undo-on-press}]
                          action)
       :container-style container-style}]]))
