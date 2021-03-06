/*
 *  Copyright 2011, 2012 Plant Breeding, Wageningen UR.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */

package nl.wur.plantbreeding.logic.swtools;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The QueryRdfEngine class handles the querying logic.
 * This class runs the given sparql query either on a given (loca) model
 * or against a remote model available in a virtuoso.
 *
 * @author Pierre-Yves Chibon -- py@chibon.fr
 */
class QueryRdfEngine {

    /** Default URI used by the engine. */
    protected String URI;
    /** boolean variable to output more information to std out. */
    protected boolean debug = false;
    /** default URL to virtuoso. */
    protected String endpoint = "http://sparql.plantbreeding.nl:8080/sparql/";
    /** logger. */
    private static final Logger LOG = Logger.getLogger(
            QueryRdfEngine.class.getName());

    /**
     * Default constructor.
     */
    public QueryRdfEngine() {
    }

    /**
     * Constructor setting the URI used.
     * @param uri the uri to set
     */
    public QueryRdfEngine(final String uri) {
        this.URI = uri;
    }

    /**
     * Returns the URI of the engine.
     * @return URI
     */
    public String getURI() {
        return URI;
    }

    /**
     * Set the engine's URI.
     * @param uri String
     */
    public void setURI(final String uri) {
        this.URI = uri;
    }

    /**
     * Return the debug mode.
     * @return boolean debug
     */
    public boolean isDebug() {
        return debug;
    }

    /**
     * Set the debug mode.
     * @param newdebug boolean
     */
    public void setDebug(final boolean newdebug) {
        this.debug = newdebug;
    }

    /**
     * Return the Service (endpoint url used for sparql query).
     * @return endpoint url of the endpoint
     */
    public String getService() {
        return endpoint;
    }

    /**
     * Set the Service (endpoint url used for sparql query).
     * @param newservice url of the endpoint
     */
    public void setService(final String newservice) {
        this.endpoint = newservice;
        LOG.log(Level.INFO, "QueryRdfEngine - Endpoint: {0}", this.endpoint);
    }

    /**
     * From a given querystring and endpoint, generate a QueryExecutionFactory
     * and return the QueryExecution.
     * @param service url of the endpoint to query
     * @param querystring sparql query
     * @return a QueryExecution object
     */
    public QueryExecution generateQuery(final String service,
            final String querystring) {
        if (debug) {
            LOG.log(Level.INFO, "Service: \n{0}", service);
            LOG.log(Level.INFO, "Query: \n{0}", querystring);
        }
        QueryExecution qexec = null;
        try {
//            qexec = QueryExecutionFactory.sparqlService(endpoint, querystring);
            qexec = new QueryEngineHTTP(endpoint, querystring);
        }
        catch (Exception ex) {
            LOG.log(Level.SEVERE, ex.getMessage());
            LOG.log(Level.SEVERE, "Query: \n{0}", querystring);
            if (this.debug) {
                LOG.log(Level.INFO, "Service: \n{0}", service);
                LOG.log(Level.SEVERE, ex.getStackTrace().toString());
            }
        }
        return qexec;
    }

    /**
     * From a given querystring, generate a QueryExecutionFactory
     * and return the QueryExecution.
     * @param querystring sparql query
     * @param model a Jena Model on which the query is going to be executed
     * @return a QueryExecution object
     */
    public QueryExecution generateLocalQuery(final String querystring,
            final Model model) {
        QueryExecution qexec = null;
        if (debug) {
            LOG.log(Level.INFO, "Query: \n{0}", querystring);
        }
        try {
            qexec = QueryExecutionFactory.create(querystring,
                    Syntax.syntaxARQ, model);
        }
        catch (Exception ex) {
            LOG.warning(ex.getMessage());
            LOG.log(Level.INFO, "Query: \n{0}", querystring);
            if (this.debug) {
                LOG.log(Level.SEVERE, ex.getStackTrace().toString());
            }
        }
        return qexec;
    }

    /**
     * Performs a given construct query to a given sparql endpoint.
     * Returns the graph generated.
     * @param service url of the endpoint to query
     * @param querystring sparql query
     * @return Model the model generated by the query
     */
    public Model remoteConstructQuery(final String service,
            final String querystring) {
        Model model = ModelFactory.createDefaultModel();
        final QueryExecution qexec = this.generateQuery(service, querystring);
        try {
            model = qexec.execConstruct();
        }
        finally {
            qexec.close();
        }
        return model;
    }

    /**
     * Performs a given construct query to the default sparql endpoint.
     * Returns the graph generated.
     * @param querystring sparql query
     * @return Model the model generated by the query
     */
    public Model remoteConstructQuery(final String querystring) {
        Model model = ModelFactory.createDefaultModel();
        final QueryExecution qexec = this.generateQuery(endpoint, querystring);
        try {
            model = qexec.execConstruct();
        }
        finally {
            qexec.close();
        }
        return model;
    }

    /**
     * Runs a remote Select Query on a given remote sparql endpoint.
     * @param service url of the endpoint to query
     * @param querystring sparql query
     * @return a ResultSet object
     */
    public ResultSet remoteSelectQuery(final String service,
            final String querystring) {
        final QueryExecution qexec = this.generateQuery(service, querystring);
        ResultSet results;
        try {
            results = qexec.execSelect();
        }
        finally {
            qexec.close();
        }
        return results;
    }

    /**
     * Runs a remote Select Query on the default sparql endpoint.
     * @param querystring a sparql query
     * @return a ResultSet object
     */
    public ResultSet remoteSelectQuery(final String querystring) {
        final QueryExecution qexec = this.generateQuery(endpoint, querystring);
        ResultSet results;
        try {
            results = qexec.execSelect();
        }
        finally {
            qexec.close();
        }
        return results;
    }

    /**
     * Runs a remote Describe Query on a given remote sparql endpoint.
     * @param service the endpoint on which the query will be executed
     * @param querystring a sparql query
     * @return Model a jena model containing the result of the describe
     */
    public Model remoteDescribeQuery(final String service,
            final String querystring) {
        final QueryExecution qexec = this.generateQuery(service, querystring);
        Model model;
        try {
            model = qexec.execDescribe();
        }
        finally {
            qexec.close();
        }
        return model;
    }

    /**
     * Runs a remote Describe Query on a given remote sparql endpoint.
     * @param querystring a sparql query
     * @return Model a Jena model against which the query will be executed
     */
    public Model remoteDescribeQuery(final String querystring) {
        final QueryExecution qexec = this.generateQuery(endpoint, querystring);
        Model model;
        try {
            model = qexec.execDescribe();
        }
        finally {
            qexec.close();
        }
        return model;
    }

    /**
     * Runs a given query on a remote endpoint and returns output of the give
     * key.
     * @param service the sparql endpoint against which to run the query
     * @param querystring a sparql query
     * @param arraylist an ArrayList in which the output of the query will be
     * stored
     * @param key thekey to retrieve the result from the query
     * @return the given ArrayList (but filled)
     */
    public ArrayList<String> remoteSelectQuery(final String service,
            final String querystring,
            final ArrayList<String> arraylist,
            final String key) {
        final QueryExecution qexec = this.generateQuery(service, querystring);
        ResultSet results;
        try {
            results = qexec.execSelect();
            this.getResults(results, key, arraylist);
        }
        finally {
            qexec.close();
        }
        return arraylist;
    }

    /**
     * Runs a given query on a remote endpoint (endpoint) and returns output of
     * the given key.
     * @param querystring a sparql query
     * @param arraylist a list in which the results of the query will be stored
     * @param key the key to retrieve the results from the query
     * @return the arraylist filled
     */
    public ArrayList<String> remoteSelectQuery(final String querystring,
            final ArrayList<String> arraylist,
            final String key) {
        final QueryExecution qexec = this.generateQuery(endpoint, querystring);
        ResultSet results;
        try {
            results = qexec.execSelect();
            this.getResults(results, key, arraylist);
        }
        finally {
            qexec.close();
        }
        return arraylist;
    }

    /**
     * Runs a given query on the remote endpoint (endpoint) and returns output
     * of the given keys in the given arraylist.
     * @param querystring a sparql query
     * @param arraylist a list in which the results of the query will be stored
     * @param keys the list of key to retrieve the results from the query
     * @return the arraylist filled
     */
    public ArrayList<ArrayList<String>> remoteSelectQuery(
            final String querystring,
            final ArrayList<ArrayList<String>> arraylist, final String[] keys) {
        final QueryExecution qexec = this.generateQuery(endpoint, querystring);
        ResultSet results;
        try {
            results = qexec.execSelect();
            this.getResults(results, keys, arraylist);
        }
        finally {
            qexec.close();
        }
        return arraylist;
    }

    /**
     * Runs a given query on a remote endpoint (endpoint) and returns output of
     * the given keys in the given arraylist.
     * @param service the sparql endpoint against which the query will be run
     * @param querystring a sparql query
     * @param arraylist a list in which the results of the query will be stored
     * @param keys the list of key to retrieve the results from the query
     * @return the arraylist filled
     */
    public ArrayList<ArrayList<String>> remoteSelectQuery(final String service,
            final String querystring,
            final ArrayList<ArrayList<String>> arraylist, final String[] keys) {
        final QueryExecution qexec = this.generateQuery(service, querystring);
        ResultSet results;
        try {
            results = qexec.execSelect();
            this.getResults(results, keys, arraylist);
        }
        finally {
            qexec.close();
        }
        return arraylist;
    }

    /**
     * Runs a given select query on a given model.
     * @param model a Jena model against which the query will be run
     * @param querystring a sparql query
     * @return a ResultSet object
     */
    public ResultSet localSelectQuery(final Model model,
            final String querystring) {
        final QueryExecution qexec =
                this.generateLocalQuery(querystring, model);
        ResultSet results;
        try {
            results = qexec.execSelect();
        }
        finally {
            qexec.close();
        }
        return results;
    }

    /**
     * Runs a Ask query on a given model and returns the returned boolean.
     * @param model a Jena model against which the query will be run
     * @param querystring a sparql query
     * @return boolean answering the ask question.
     */
    public boolean localAskQuery(final Model model, final String querystring) {
        final QueryExecution qexec =
                this.generateLocalQuery(querystring, model);
        boolean results;
        try {
            results = qexec.execAsk();
        }
        finally {
            qexec.close();
        }
        return results;
    }

    /**
     * Runs a given construct query on a given model.
     * @param model a Jena model against which the query will be run
     * @param querystring a sparql query
     * @return Model a Jena model resulting from the construct query
     */
    public Model localConstructQuery(final Model model,
            final String querystring) {
        final QueryExecution qexec =
                this.generateLocalQuery(querystring, model);
        Model mod;
        try {
            mod = qexec.execConstruct();
//            this.printResults(results, "o");
        }
        finally {
            qexec.close();
        }
        return mod;
    }

    /**
     * Runs a given describe query on a given model.
     * @param model a Jena model against which the query will be run
     * @param querystring a sparql query
     * @return Model a Jena model resulting from the construct query
     */
    public Model localDescribeQuery(final Model model,
            final String querystring) {
        final QueryExecution qexec =
                this.generateLocalQuery(querystring, model);
        Model mod;
        try {
            mod = qexec.execDescribe();
        }
        finally {
            qexec.close();
        }
        return mod;
    }

    /**
     * Runs a given query on a given model and returns output of the given key
     * in the given arraylist.
     * @param model a Jena model against which the query will be run
     * @param querystring a sparql query
     * @param arraylist a list in which the results of the query will be stored
     * @param key the key to retrieve the results from the query
     * @return the arraylist filled
     */
    public ArrayList<String> localSelectQuery(final Model model,
            final String querystring,
            final ArrayList<String> arraylist,
            final String key) {
        final QueryExecution qexec =
                this.generateLocalQuery(querystring, model);
        ResultSet results;
        try {
            results = qexec.execSelect();
            this.getResults(results, key, arraylist);
        }
        finally {
            qexec.close();
        }
        return arraylist;
    }

    /**
     * Runs a given query on a given model and returns output of the given keys
     * in the given arraylist.
     * @param model a Jena model against which the query will be run
     * @param querystring a sparql query
     * @param arraylist a list in which the results of the query will be stored
     * @param keys the list of key to retrieve the results from the query
     * @return the arraylist filled
     */
    public ArrayList<ArrayList<String>> localSelectQuery(final Model model,
            final String querystring,
            final ArrayList<ArrayList<String>> arraylist,
            final String[] keys) {
        final QueryExecution qexec =
                this.generateLocalQuery(querystring, model);
        ResultSet results;
        try {
            results = qexec.execSelect();
            this.getResults(results, keys, arraylist);
        }
        finally {
            qexec.close();
        }
        return arraylist;
    }

    /**
     * Returns the output from the given key in the given ResultSet in the given
     * ArrayList.
     * @param results a ResultSet object
     * @param keys a list of key
     * @param arraylist a list of list in which to store the results extracted
     * @return the arraylist filled
     */
    public ArrayList<ArrayList<String>> getResults(
            final ResultSet results,
            final String[] keys,
            final ArrayList<ArrayList<String>> arraylist) {
        int cnt = 0;

        while (results.hasNext()) {
            final QuerySolution soln = results.nextSolution();
            final ArrayList<String> tmp = new ArrayList<String>(keys.length);
            for (String key : keys) {
                // Get a result variable by name.
                final RDFNode node = soln.get(key);
                if (node != null) {
                    tmp.add(node.toString());
                }
                if (debug) {
                    if (node != null) {
                        String[] val = {key, node.toString()};
                        LOG.log(Level.INFO, "{0} : {1}", val);
                    }
                    LOG.log(Level.INFO, "{0} is null", key);
                }
            }
            arraylist.add(tmp);
            cnt += 1;
        }
        if (debug) {
            LOG.log(Level.INFO, "{0} statements in the ResultSet", cnt);
        }
        return arraylist;
    }

    /**
     * Returns the output from the given keys in the given ResultSet in the
     * given ArrayList.
     * @param results a ResultSet object
     * @param key the key to retrieve the result from the ResultSet
     * @param arraylist the list in which the results will be stored
     * @return the arraylist filled
     */
    public ArrayList<String> getResults(final ResultSet results,
            final String key,
            final ArrayList<String> arraylist) {
        int cnt = 0;
        while (results.hasNext()) {
            final QuerySolution soln = results.nextSolution();
            if (debug) {
                LOG.log(Level.INFO, "soln: {0}", soln.toString());
            }
            // Get a result variable by name.
            final RDFNode node = soln.get(key);
            if (node != null) {
                arraylist.add(node.toString());
            }
            cnt += 1;
        }
        if (debug) {
            LOG.log(Level.INFO, "{0} statements in the ResultSet", cnt);
        }
        return arraylist;
    }

    /**
     * Print the keyword from the ResultSet.
     * (generated by a query)
     * @param results a ResultSet object
     * @param key a key to retrieve results from the ResultSet
     */
    public void printResults(final ResultSet results, final String key) {
        LOG.log(Level.INFO, "{0}", key);
        final String[] vals = {key};
        this.printResults(results, vals);
    }

    /**
     * Print all the keywords from a ResultSet.
     * (generated by a query)
     * @param results a ResultSet object
     * @param keys a list of keys to retrieve results from the ResultSet
     */
    public void printResults(final ResultSet results, final String[] keys) {
        int cnt = 0;
        while (results.hasNext()) {
            final QuerySolution soln = results.nextSolution();
            for (String key : keys) {
                // Get a result variable by name.
                final RDFNode node = soln.get(key);
                if (node != null) {
                    final Object[] objs = {key, node.toString()};
                    LOG.log(Level.INFO, "{0}:{1}", objs);
                }
            }
            cnt += 1;
        }
        if (debug) {
            LOG.log(Level.INFO, "{0} statements in the ResultSet", cnt);
        }
    }
}
