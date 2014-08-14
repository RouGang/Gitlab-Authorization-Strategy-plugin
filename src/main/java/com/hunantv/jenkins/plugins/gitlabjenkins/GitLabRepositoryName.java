/**
 * @Title: GitLabRepositoryName.java.
 * @Package com.hunantv.jenkins.plugins.gitlabjenkins
 * Copyright: Copyright (c) 2014年6月29日
 * Company:湖南创发科技有限责任公司
 * @author Comsys-hrg
 * @date 2014年6月29日 下午9:32:17
 * @version V1.0
 */
package com.hunantv.jenkins.plugins.gitlabjenkins;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>Class Name: GitLabRepositoryName.</p>
 * <p>Description: 类功能说明</p>
 * <p>Sample: 该类的典型使用方法和用例</p>
 * <p>Author: hrg</p>
 * <p>Date: 2014年6月29日</p>
 * <p>Modified History: 修改记录，格式(Name)  (Version)  (Date) (Reason & Contents)</p>
 */
public class GitLabRepositoryName {
    private static final Pattern[] URL_PATTERNS = {
        /**
         * The first set of patterns extract the host, owner and repository names
         * from URLs that include a '.git' suffix, removing the suffix from the
         * repository name.
         */
            Pattern.compile("git@(.+):([^/]+)/([^/]+)\\.git"),
            Pattern.compile("https?://[^/]+@([^/]+)/([^/]+)/([^/]+)\\.git"),
            Pattern.compile("https?://([^/]+)/([^/]+)/([^/]+)\\.git"),
            Pattern.compile("git://([^/]+)/([^/]+)/([^/]+)\\.git"),
            Pattern.compile("ssh://git@([^/]+)/([^/]+)/([^/]+)\\.git"),
        /**
         * The second set of patterns extract the host, owner and repository names
         * from all other URLs. Note that these patterns must be processed *after*
         * the first set, to avoid any '.git' suffix that may be present being included
         * in the repository name.
         */
            Pattern.compile("git@(.+):([^/]+)/([^/]+)/?"),
            Pattern.compile("https?://[^/]+@([^/]+)/([^/]+)/([^/]+)/?"),
            Pattern.compile("https?://([^/]+)/([^/]+)/([^/]+)/?"),
            Pattern.compile("git://([^/]+)/([^/]+)/([^/]+)/?"),
            Pattern.compile("ssh://git@([^/]+)/([^/]+)/([^/]+)/?")
        };

        /**
         * Create {@link GitHubRepositoryName} from URL
         *
         * @param url
         *            must be non-null
         * @return parsed {@link GitHubRepositoryName} or null if it cannot be
         *         parsed from the specified URL
         */
        public static GitLabRepositoryName create(final String url) {
            LOGGER.log(Level.FINE, "Constructing from URL {0}", url);
            for (Pattern p : URL_PATTERNS) {
                Matcher m = p.matcher(url.trim());
                if (m.matches()) {
                    LOGGER.log(Level.FINE, "URL matches {0}", m);
                    GitLabRepositoryName ret = new GitLabRepositoryName(m.group(1), m.group(2),
                            m.group(3));
                    LOGGER.log(Level.FINE, "Object is {0}", ret);
                    return ret;
                }
            }
            LOGGER.log(Level.WARNING, "Could not match URL {0}", url);
            return null;
        }

        public final String host, userName, repositoryName;

        public GitLabRepositoryName(String host, String userName, String repositoryName) {
            this.host = host;
            this.userName = userName;
            this.repositoryName = repositoryName;
        }

        @Override
        public String toString() {
            return "GitHubRepository[host="+host+",username="+userName+",repository="+repositoryName+"]";
        }

        private static final Logger LOGGER = Logger.getLogger(GitLabRepositoryName.class.getName());
}
