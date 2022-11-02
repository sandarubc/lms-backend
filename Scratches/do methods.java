resp.getWriter().println("Members:doGet()");
        if (req.getPathInfo()==null||req.getPathInfo().equals("/")) {
            String query = req.getParameter("q");
            String size = req.getParameter("size");
            String page = req.getParameter("page");
            if(query!=null && size!=null && page!=null){
                if(!size.matches("\\d+")||!page.matches("\\d+")){
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST,"Invalid page or size");
                }
                else{
                    searchPaginatedMembers(query, Integer.parseInt(size),Integer.parseInt(page),resp);
                }

            } else if (query!=null) {
                resp.getWriter().println("<h1>search members</h1>");
                searchMembers(query,resp);

            } else if (size!=null && page !=null) {

                paginatedMembers(Integer.parseInt(size),Integer.parseInt(page),resp);
            }else{
                resp.getWriter().println("<h1>load all members</h1>");

            }
        }
        else{
            Pattern compile = Pattern.compile("^/[A-Fa-f0-9]{8}-([A-Fa-f0-9]{4}-){3}[A-Fa-f0-9]{12}/?$");
            Matcher matcher = compile.matcher(req.getPathInfo());
            if(matcher.matches()){
                resp.getWriter().printf("<h1>Get a member details %s</h1>",matcher.group());
                System.out.println("Group 0 "+matcher.group(0));
                System.out.println("Group 1 "+matcher.group(1));
                System.out.println("Group 2 "+matcher.group(2));
            }
            else{
                resp.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED,"Expected valid UUID");
            }
        }

    }

    private void loadAllMembers(HttpServletResponse response) throws IOException {
        response.getWriter().println("<h1>Load all members</h1>");

    }

    private void loadMembersByPage(HttpServletResponse response) throws IOException {
        response.getWriter().println("<h1>Load all members by page</h1>");
    }

    private void loadPaginatedMembers(int size, int age, HttpServletResponse response) throws IOException {
        response.getWriter().println("<h1>Load all paginated members</h1>");
    }
    private void searchMembers(String query, HttpServletResponse response) throws IOException {
        response.getWriter().println("<h1>search all members</h1>");
    }

    private void searchPaginatedMembers(String query, int size,int page, HttpServletResponse response) throws IOException {
        response.getWriter().println("<h1>search paginated members</h1>");
    }
    private void getMemberDetails(String memberID, HttpServletResponse response) throws IOException {
        response.getWriter().println("<h1>Get member details</h1>");
    }

    private void paginatedMembers(int size, int page, HttpServletResponse resp) throws IOException {
        resp.getWriter().println("<h1>Paginated members</h1>");
    }
