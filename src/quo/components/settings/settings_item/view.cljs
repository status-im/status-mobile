(ns quo.components.settings.settings-item.view
  (:require
    [clojure.string :as string]
    [quo.components.avatars.icon-avatar :as icon-avatar]
    [quo.components.avatars.user-avatar.view :as user-avatar]
    [quo.components.buttons.button.view :as button]
    [quo.components.icon :as icon]
    [quo.components.list-items.preview-list.view :as preview-list]
    [quo.components.markdown.text :as text]
    [quo.components.selectors.selectors.view :as selectors]
    [quo.components.settings.settings-item.style :as style]
    [quo.components.tags.context-tag.view :as context-tag]
    [quo.components.tags.status-tags :as status-tags]
    [quo.components.utilities.token.view :as token]
    [quo.foundations.colors :as colors]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]
    [utils.i18n :as i18n]))

(defn status-description
  [{:keys [description-props blur?]}]
  (let [theme                  (quo.theme/use-theme)
        {:keys [online? text]} description-props]
    [rn/view {:style style/status-container}
     [rn/view {:style (style/status-dot online? blur?)}]
     [text/text
      {:size  :paragraph-2
       :style (style/color blur? theme)}
      (if online? (i18n/label :t/online-now) text)]]))

(defn text-description
  [{:keys [description-props blur?]}]
  (let [theme               (quo.theme/use-theme)
        {:keys [text icon]} description-props]
    [rn/view
     {:style (style/sub-container :center)}
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

(defn emoji-component
  [image-props]
  [rn/text
   (when image-props (string/trim image-props))])

(defn image-component
  [{:keys [image image-props description tag blur?]}]
  (let [theme (quo.theme/use-theme)]
    [rn/view
     {:style (style/image-container description tag image)}
     (case image
       :icon        [icon/icon image-props (style/color blur? theme)]
       :avatar      [user-avatar/user-avatar image-props]
       :icon-avatar [icon-avatar/icon-avatar image-props]
       :token       [token/view image-props]
       :emoji       [emoji-component image-props]
       nil)]))

(defn tag-component
  [{:keys [tag tag-props]}]
  (case tag
    :positive [status-tags/status-tag
               {:status          {:type :positive}
                :label           (:label tag-props)
                :no-icon?        true
                :size            :small
                :container-style style/status-tag-container}]
    :context  [context-tag/view
               (merge tag-props
                      {:type            :icon
                       :size            24
                       :container-style {:margin-top 8
                                         :align-self :flex-start}})]
    nil))

(defn label-component
  [{:keys [label label-props label-icon-props blur? theme preview-size]}]
  [rn/view {:accessibility-label :label-component}
   (case label
     :text    [text/text
               {:style (style/color blur? theme)}
               label-props]
     :color   [rn/view
               {:style (style/label-dot label-props)}]
     :preview [preview-list/view {:type (:type label-props) :size (or preview-size :size-24)}
               (:data label-props)]
     :icon    [icon/icon label-props label-icon-props]
     nil)])

(defn action-component
  [{:keys [action action-props blur? theme]}]
  [rn/view {:style {:margin-left 12}}
   (case action
     :arrow    [icon/icon (or (:icon action-props) :i/chevron-right) (style/color blur? theme)]
     :button   [button/button
                (merge action-props
                       {:type :outline
                        :size 24})
                (:button-text action-props)]
     :selector [selectors/view action-props]
     nil)])

(defn view
  [{:keys [title on-press action-props accessibility-label blur? container-style content] :as props}]
  [rn/pressable
   {:style               (merge style/container container-style)
    :on-press            (or on-press (:on-change action-props))
    :accessibility-label accessibility-label}
   [rn/view
    {:style {:flex-direction  :row
             :justify-content :space-between}}
    [rn/view {:style (style/left-sub-container props)}
     [image-component props]
     [rn/view {:style (style/left-container (:image props))}
      [text/text
       {:weight :medium
        :style  {:color (when blur? colors/white)}}
       title]
      [description-component props]
      [tag-component props]]]
    [rn/view {:style (style/sub-container (:alignment action-props))}
     [label-component props]
     [action-component props]]]
   (when content
     content)])
