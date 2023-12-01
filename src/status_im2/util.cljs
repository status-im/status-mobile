(ns status-im2.util
  (:require-macros [status-im2.util :as um])
  (:require [re-frame.core :as rf]
            [react-native.platform :as platform]
            [status-im.utils.utils :as utils]
            [status-im2.contexts.shell.jump-to.utils :as shell.utils]
            [utils.security.core :as security]))

(defn do-on-event* [target-event callback]
  (let [cb-id (gensym "do-on-cb-fn")]
    (rf/add-post-event-callback
      cb-id
      (fn [event _]
        (when (= event target-event)
          (rf/remove-post-event-callback cb-id)
          (callback))))
    :ok))

(defn do-on-event-name* [target-event-name callback]
  (let [cb-id (gensym "do-on-cb-fn")]
    (rf/add-post-event-callback
      cb-id
      (fn [[event-name] _]
        (when (= event-name target-event-name)
          (rf/remove-post-event-callback cb-id)
          (callback))))
    :ok))

(defn run-init-scenario! []
  (um/run-scenario
    (when-let [blur-show-fn @status-im2.contexts.onboarding.common.overlay.view/blur-show-fn-atom]
      (blur-show-fn))
    (rf/dispatch [:open-modal :new-to-status])
    {::wait 1000}
    (rf/dispatch [:onboarding-2/navigate-to-create-profile])
    {::wait 1000}
    (rf/dispatch [:onboarding-2/profile-data-set
                  {:image-path nil, :display-name "lambdam", :color :blue}])
    {::wait 1000}
    (rf/dispatch [:onboarding-2/password-set (security/mask-data "qwertyuiop")])
    ;; {::on-event-name :onboarding-2/navigate-to-identifiers}
    {::on-event [:navigate-to-within-stack [:identifiers :new-to-status]]}
    {::wait 1000}
    (rf/dispatch [:navigate-to-within-stack [:enable-notifications :new-to-status]])
    {::wait 1000}
    (shell.utils/change-selected-stack-id :communities-stack true nil)
    (rf/dispatch [:push-notifications/switch true platform/ios?])
    (rf/dispatch [:navigate-to-within-stack
                  [:welcome :enable-notifications]])
    {::wait 1000}
    (rf/dispatch [:init-root :shell-stack])
    (rf/dispatch [:universal-links/process-stored-event])
    {::wait 2000}
    (utils/show-popup "Scenarios" "Profile creation scenario finished")))

(comment

  ;; Nested example
  (do (when-let [blur-show-fn @status-im2.contexts.onboarding.common.overlay.view/blur-show-fn-atom]
        (blur-show-fn))
      (rf/dispatch [:open-modal :new-to-status])
      (um/wait 1000
        (rf/dispatch [:onboarding-2/navigate-to-create-profile])
        (um/wait 1000
          (rf/dispatch [:onboarding-2/profile-data-set
                        {:image-path nil, :display-name "lambdam", :color :blue}])
          (um/wait 1000
            (rf/dispatch [:onboarding-2/password-set (security/mask-data "qwertyuiop")])
            (um/do-on-event [:navigate-to-within-stack [:identifiers :new-to-status]]
              (um/wait 1000
                (rf/dispatch [:navigate-to-within-stack [:enable-notifications :new-to-status]])
                (um/wait 1000
                  (shell.utils/change-selected-stack-id :communities-stack true nil)
                  (rf/dispatch [:push-notifications/switch true platform/ios?])
                  (rf/dispatch [:navigate-to-within-stack
                                [:welcome :enable-notifications]])
                  (um/wait 1000
                    (rf/dispatch [:init-root :shell-stack])
                    (rf/dispatch [:universal-links/process-stored-event])
                    (um/wait 2000
                      (utils/show-popup "Scenarios" "Profile creation scenario finished"))))))))))

  ;; "Aligned" example (which expands to the same code than the previous example)
  (run-init-scenario!)

  )
