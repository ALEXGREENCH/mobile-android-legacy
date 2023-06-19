package uk.openvk.android.legacy.api.entities;

/** OPENVK LEGACY LICENSE NOTIFICATION
 *
 *  This program is free software: you can redistribute it and/or modify it under the terms of
 *  the GNU Affero General Public License as published by the Free Software Foundation, either
 *  version 3 of the License, or (at your option) any later version.
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License along with this
 *  program. If not, see https://www.gnu.org/licenses/.
 *
 *  Source code: https://github.com/openvk/mobile-android-legacy
 **/

public class InstanceStatistics {
    public int users_count;
    public int online_users_count;
    public int active_users_count;
    public int groups_count;
    public int wall_posts_count;
    public InstanceStatistics(int users_count, int online_users_count, int active_users_count,
                              int groups_count, int wall_posts_count) {
        this.users_count = users_count;
        this.online_users_count = online_users_count;
        this.active_users_count = active_users_count;
        this.groups_count = groups_count;
        this.wall_posts_count = wall_posts_count;
    }
}