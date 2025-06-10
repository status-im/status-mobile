(ns status-im.contexts.onboarding.interceptors
  (:require
    [re-frame.interceptor :as interceptor]
    [react-native.mmkv :as mmkv]))

(defn inject-local-profile-storage
  [context]
  (let [db            (interceptor/get-coeffect context :db)
        key-uid       (get-in db [:profile/profile :key-uid])
        local-profile (mmkv/get-object key-uid)]
    (assoc-in context
     [:coeffects :local-profile-storage]
     local-profile)))

(def local-profile-storage-interceptor
  (interceptor/->interceptor
   :id     :local-profile-storage-interceptor
   :before inject-local-profile-storage))
