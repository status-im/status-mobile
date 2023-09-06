(ns quo2.components.settings.settings-item.view
  (:require
    [quo2.components.icon :as icon]
    [quo2.components.list-items.preview-list.view :as preview-list]
    [quo2.components.selectors.selectors.view :as selectors]
    [quo2.components.buttons.button.view :as button]
    [quo2.components.markdown.text :as text]
    [quo2.theme :as quo.theme]
    [react-native.core :as rn]
    [quo2.components.avatars.icon-avatar :as icon-avatar]
    [quo2.components.tags.status-tags :as status-tags]
    [quo2.components.tags.context-tag.view :as context-tag]
    [quo2.components.settings.settings-item.style :as style]
    [quo2.components.avatars.user-avatar.view :as user-avatar]
    [utils.i18n :as i18n]))

(defn status-description
  [{:keys [description-props blur? theme]}]
  (let [{:keys [online? text]} description-props]
    [rn/view {:style style/status-container}
     [rn/view {:style (style/status-dot online? blur?)}]
     [text/text
      {:size  :paragraph-2
       :style (style/color blur? theme)}
      (if online? (i18n/label :t/online-now) text)]]))

(defn text-description
  [{:keys [description-props blur? theme]}]
  (let [{:keys [text icon]} description-props]
    [rn/view
     {:style style/sub-container}
     [text/text
      {:size  :paragraph-2
       :style (style/color blur? theme)}
      text]
     (when icon
       [icon/icon icon
        (merge (style/color blur? theme)
               {:size            16
                :container-style {:margin-left 4}})])]))

(defn description-component
  [{:keys [description] :as props}]
  (case description
    :text           [text-description props]
    :text-plus-icon [text-description props]
    :status         [status-description props]
    nil))

(defn image-component
  [{:keys [image image-props description tag blur? theme]}]
  [rn/view
   {:style (style/image-container description tag image)}
   (case image
     :icon        [icon/icon image-props (style/color blur? theme)]
     :avatar      [user-avatar/user-avatar image-props]
     :icon-avatar [icon-avatar/icon-avatar image-props]
     nil)])

(defn tag-component
  [{:keys [tag tag-props]}]
  (case tag
    :positive [status-tags/status-tag
               {:status          {:type :positive}
                :label           (i18n/label :t/positive)
                :size            :small
                :container-style {:margin-top 8}}]
    :context  [context-tag/view
               (merge tag-props
                      {:type            :icon
                       :size            24
                       :container-style {:margin-top 8
                                         :align-self :flex-start}})]
    nil))

(defn label-component
  [{:keys [label label-props blur? theme]}]
  [rn/view {:accessibility-label :label-component}
   (case label
     :text    [text/text
               {:style (style/color blur? theme)}
               label-props]
     :color   [rn/view
               {:style (style/label-dot label-props)}]
     :preview [preview-list/view {:type (:type label-props)} (:data label-props)]
     nil)])

(defn action-component
  [{:keys [action action-props blur? theme]}]
  [rn/view {:style {:margin-left 12}}
   (case action
     :arrow    [icon/icon :i/chevron-right (style/color blur? theme)]
     :button   [button/button
                {:type     :outline
                 :size     24
                 :on-press (:on-press action-props)}
                (:button-text action-props)]
     :selector [selectors/toggle action-props]
     nil)])

(defn- internal-view
  [{:keys [title on-press accessibility-label] :as props}]
  [rn/pressable
   {:style               (style/container props)
    :on-press            on-press
    :accessibility-label accessibility-label}
   [rn/view {:style style/sub-container}
    [image-component props]
    [rn/view {:style style/left-container}
     [text/text {:weight :medium} title]
     [description-component props]
     [tag-component props]]]
   [rn/view {:style style/sub-container}
    [label-component props]
    [action-component props]]])

(def view (quo.theme/with-theme internal-view))
