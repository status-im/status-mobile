(ns quo2.components.list-items.community.view
  (:require [quo2.components.community.community-view :as community-view]
            [quo2.components.counter.counter.view :as counter]
            [quo2.components.icon :as icons]
            [quo2.components.list-items.community.style :as style]
            [quo2.components.markdown.text :as text]
            [quo2.foundations.colors :as colors]
            [quo2.theme :as quo.theme]
            [react-native.core :as rn]
            [reagent.core :as reagent]))

(defn- logo-component
  [logo]
  [rn/image {:source logo :style style/logo}])

(defn- title-component
  [{:keys [title type] :as props}]
  [text/text
   {:weight              :semi-bold
    :size                :paragraph-1
    :accessibility-label :community-item-title
    :number-of-lines     1
    :ellipsize-mode      :tail
    :style               (style/title props)}
   (if (= type :share)
     (str "# " title)
     title)])

(defn- subtitle-component
  [subtitle blur? theme]
  [text/text
   {:size                :paragraph-2
    :number-of-lines     1
    :accessibility-label :community-item-subtitle
    :style               (style/subtitle blur? theme)}
   subtitle])

(defn- notification-dot
  [blur? theme]
  [rn/view
   {:style               (style/notification-dot blur? theme)
    :accessibility-label :info-notification-dot}])

(defn- info-component
  [{:keys [customization-color info type blur? locked? on-press-info theme tokens unread-count]}]
  (let [component
        (cond
          (and (= type :discover) (= info :token-gated) (seq tokens))
          [community-view/permission-tag-container
           {:locked?  locked?
            :tokens   tokens
            :on-press on-press-info
            :theme    theme
            :blur?    blur?}]

          (and (= type :engage) (= info :mention) (pos? unread-count))
          [counter/counter
           {:type                :default
            :customization-color customization-color}
           unread-count]

          (and (= type :engage) (= info :notification))
          [notification-dot blur? theme]

          (and (= type :engage) (= info :muted))
          [icons/icon :i/muted
           {:accessibility-label :info-muted
            :color               (colors/theme-colors colors/neutral-40 colors/neutral-60 theme)}]

          (and (= type :engage) (= info :token-gated))
          [icons/icon (if locked? :i/locked :i/unlocked)
           {:accessibility-label :info-token-gated
            :color               (colors/theme-colors colors/neutral-50 colors/neutral-40 theme)}]

          (and (= type :engage) (= info :navigation))
          [icons/icon :i/chevron-right
           {:accessibility-label :info-navigation
            :color               (colors/theme-colors colors/neutral-50 colors/neutral-40 theme)}])]
    (when component
      [rn/view
       {:accessibility-label :community-item-info
        :style               {:margin-left 10}}
       component])))

(defn- view-internal
  "Options:

  :type - :discover/engage/share
  :info - keyword - Acceptable values vary based on the :type option.

  :title - string
  :logo - image resource
  :theme - :light/dark

  :container-style - style map - Override styles in top-level view component.

  :members - {:members-count number, :active-count number} - When non-nil, the
  statistics component will be shown.

  :tokens - A sequence of maps, e.g. [{:id 1 :group [{:id 1 :token-icon an-icon}]}].

  :locked? - boolean - When true, the permission-tag icon or the token gated
  icon will appear as closed.

  :blur? - boolean - It will be taken into account when true and dark mode is
  enabled.

  :customization-color - color - It will be passed down to components that
  should vary based on a custom color.

  :on-press/:on-long-press - fn - Used by the top-level pressable component.

  :on-press-info - fn - Will be called when the info component is pressed.

  :unread-count - number - When the info is :mention, it will be used to show
  the number of unread mentions.
  "
  []
  (let [pressed? (reagent/atom false)]
    (fn [{:keys [members type info tokens locked? title subtitle
                 logo blur? customization-color
                 on-press on-long-press on-press-info
                 container-style unread-count theme]}]
      [rn/pressable
       {:accessibility-label :container
        :on-press-in         (fn [] (reset! pressed? true))
        :on-press            on-press
        :on-long-press       on-long-press
        :on-press-out        (fn [] (reset! pressed? false))
        :style               (merge (style/container {:blur?               blur?
                                                      :customization-color customization-color
                                                      :info                info
                                                      :type                type
                                                      :pressed?            @pressed?
                                                      :theme               theme})
                                    container-style)}
       [logo-component logo]
       [rn/view {:style {:flex 1}}
        [title-component
         {:blur? blur?
          :info  info
          :theme theme
          :title title
          :type  type}]
        (when (and (= type :share) subtitle)
          [subtitle-component subtitle blur? theme])
        (when (and members (= type :discover))
          [community-view/community-stats-column
           {:type          :list-view
            :theme         theme
            :blur?         blur?
            :members-count (:members-count members)
            :active-count  (:active-count members)}])]
       [info-component
        {:blur?               blur?
         :customization-color customization-color
         :info                info
         :type                type
         :locked?             locked?
         :on-press-info       on-press-info
         :theme               theme
         :tokens              tokens
         :unread-count        unread-count}]])))

(def view (quo.theme/with-theme view-internal))
